package com.freshiq.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshiq.app.data.local.ScanHistoryEntity
import com.freshiq.app.data.model.PredictionResponseDto
import com.freshiq.app.data.repository.ScanHistoryRepository
import com.freshiq.app.data.repository.ScanSessionManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class HistoryFilter(val label: String) {
    ALL("All"),
    URGENT("Needs Attention"),
    CHILLED("Chilled"),
    FRESH("Fresh & Firm"),
    OVERRIPE("Overripe")
}

enum class HistorySort(val label: String) {
    NEWEST("Newest Scans First"),
    OLDEST("Oldest Scans First"),
    RUL_ASC("RUL: Shortest First (Urgent)"),
    RUL_DESC("RUL: Longest First (Holding)"),
    CONFIDENCE_DESC("Confidence: Highest First")
}

data class HistoryTelemetry(
    val total: Int = 0,
    val urgent: Int = 0,
    val chilled: Int = 0,
    val fresh: Int = 0,
    val overripe: Int = 0,
    val wasteSaved: String = "0.00",
    val co2Avoided: String = "0.0"
)

data class ScanHistoryItemUi(
    val id: String,
    val timestamp: Long,
    val formattedDate: String,
    val thumbnailPath: String,
    val storageCondition: String,
    val storageConditionLabel: String,
    val prediction: PredictionResponseDto,
    val stage: Int,
    val stageLabel: String,
    val confidencePercent: String,
    val ambientRulDays: Float,
    val isUrgent: Boolean,
    val isTerminal: Boolean,
    val statusBadgeText: String,
    val hasRefrigerationGain: Boolean
)

data class HistoryControls(
    val query: String = "",
    val filter: HistoryFilter = HistoryFilter.ALL,
    val sort: HistorySort = HistorySort.NEWEST,
    val deleteCandidateId: String? = null,
    val showClearAllDialog: Boolean = false,
    val errorMessage: String? = null
)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val rawScans: List<ScanHistoryItemUi> = emptyList(),
    val filteredScans: List<ScanHistoryItemUi> = emptyList(),
    val telemetry: HistoryTelemetry = HistoryTelemetry(),
    val searchQuery: String = "",
    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val selectedSort: HistorySort = HistorySort.NEWEST,
    val deleteCandidateId: String? = null,
    val showClearAllDialog: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HistoryViewModel(
    private val scanHistoryRepository: ScanHistoryRepository,
    private val scanSessionManager: ScanSessionManager,
    private val gson: Gson,
    coroutineScope: CoroutineScope? = null
) : ViewModel() {

    @Inject
    constructor(
        scanHistoryRepository: ScanHistoryRepository,
        scanSessionManager: ScanSessionManager,
        gson: Gson
    ) : this(scanHistoryRepository, scanSessionManager, gson, null)

    private val scope: CoroutineScope = coroutineScope ?: viewModelScope

    private val _controls = MutableStateFlow(HistoryControls())
    private val _dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())

    val uiState: StateFlow<HistoryUiState> = combine(
        scanHistoryRepository.getScanHistory(),
        _controls
    ) { rawEntities, controls ->
        val parsedItems = rawEntities.mapNotNull { entity ->
            parseEntityToUiItem(entity)
        }

        val telemetry = computeTelemetry(parsedItems)
        val filteredItems = filterAndSortItems(parsedItems, controls.query, controls.filter, controls.sort)

        HistoryUiState(
            isLoading = false,
            rawScans = parsedItems,
            filteredScans = filteredItems,
            telemetry = telemetry,
            searchQuery = controls.query,
            selectedFilter = controls.filter,
            selectedSort = controls.sort,
            deleteCandidateId = controls.deleteCandidateId,
            showClearAllDialog = controls.showClearAllDialog,
            errorMessage = controls.errorMessage
        )
    }.stateIn(
        scope = scope,
        started = if (coroutineScope != null) SharingStarted.Eagerly else SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    private fun parseEntityToUiItem(entity: ScanHistoryEntity): ScanHistoryItemUi? {
        return try {
            val prediction = gson.fromJson(entity.predictionJson, PredictionResponseDto::class.java)
                ?: return null
            val isBanana = prediction.foodType == "banana"
            val isMango = prediction.foodType == "mango"
            val isClassificationOnly = isBanana || isMango || !prediction.rulAvailable
            val stage = prediction.ripeness.predictedRipeningStage
            val stageLabel = prediction.ripeness.stageLabel
            val confidence = prediction.ripeness.confidence

            val ambientRul = if (isClassificationOnly) {
                0.0f
            } else {
                prediction.scenarios?.get("ambient")?.estimatedRulDays
                    ?: prediction.scenarios?.values?.firstOrNull()?.estimatedRulDays ?: 0.0f
            }

            val isTerminal = when {
                isBanana -> false
                isMango -> stage == 4
                else -> stage == 5
            }
            val isUrgent = when {
                isBanana -> false
                isMango -> stage == 3
                else -> (stage >= 4 || ambientRul <= 2.0f) && !isTerminal
            }
            val hasRefrigerationGain = if (isClassificationOnly) false else ((prediction.refrigerationExtensionGainDays ?: 0f) > 0f)

            val statusBadgeText = when {
                isBanana -> when (stage) {
                    2 -> "Ripe (Ready to Eat)"
                    1 -> "Semi-ripe (Ripening)"
                    else -> "Unripe (Storage/Holding)"
                }
                isMango -> when (stage) {
                    4 -> "Perished (Discard)"
                    3 -> "Overripe (Consume Immediately)"
                    2 -> "Fully Ripe (Peak Eating)"
                    1 -> "Semiripe (Ripening)"
                    else -> "Unripe (Storage/Holding)"
                }
                else -> when {
                    isTerminal -> "Overripe (Senescent)"
                    isUrgent -> "Consume Soon (Peak Window)"
                    stage == 3 -> "Firm Ripe (Slicing Ready)"
                    else -> "Active Shelf Life"
                }
            }

            ScanHistoryItemUi(
                id = entity.id,
                timestamp = entity.timestamp,
                formattedDate = _dateFormat.format(Date(entity.timestamp)),
                thumbnailPath = entity.thumbnailPath,
                storageCondition = entity.storageCondition,
                storageConditionLabel = formatStorageConditionLabel(entity.storageCondition),
                prediction = prediction,
                stage = stage,
                stageLabel = stageLabel,
                confidencePercent = String.format(Locale.US, "%.1f", confidence * 100f),
                ambientRulDays = ambientRul,
                isUrgent = isUrgent,
                isTerminal = isTerminal,
                statusBadgeText = statusBadgeText,
                hasRefrigerationGain = hasRefrigerationGain
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun formatStorageConditionLabel(condition: String?): String {
        if (condition.isNullOrBlank()) return "Ambient Counter (~20°C)"
        val lower = condition.lowercase()
        return when {
            lower.contains("10") -> "Cold Storage (10°C)"
            lower.contains("4") -> "Refrigerator (4°C)"
            lower.contains("20") -> "Controlled (20°C)"
            else -> "Ambient Counter (~20°C)"
        }
    }

    private fun computeTelemetry(items: List<ScanHistoryItemUi>): HistoryTelemetry {
        val total = items.size
        var urgentCount = 0
        var chilledCount = 0
        var freshCount = 0
        var overripeCount = 0

        items.forEach { item ->
            when {
                item.isTerminal -> overripeCount++
                item.isUrgent -> urgentCount++
                else -> freshCount++
            }
            if (item.hasRefrigerationGain) {
                chilledCount++
            }
        }

        val wasteSaved = String.format(Locale.US, "%.2f", total * 3.25)
        val co2Avoided = String.format(Locale.US, "%.1f", total * 0.45)

        return HistoryTelemetry(
            total = total,
            urgent = urgentCount,
            chilled = chilledCount,
            fresh = freshCount,
            overripe = overripeCount,
            wasteSaved = wasteSaved,
            co2Avoided = co2Avoided
        )
    }

    private fun filterAndSortItems(
        items: List<ScanHistoryItemUi>,
        query: String,
        filter: HistoryFilter,
        sort: HistorySort
    ): List<ScanHistoryItemUi> {
        val filtered = items.filter { item ->
            val matchesFilter = when (filter) {
                HistoryFilter.ALL -> true
                HistoryFilter.URGENT -> item.isUrgent
                HistoryFilter.OVERRIPE -> item.isTerminal
                HistoryFilter.CHILLED -> item.hasRefrigerationGain
                HistoryFilter.FRESH -> !item.isUrgent && !item.isTerminal
            }

            val matchesSearch = if (query.isBlank()) {
                true
            } else {
                val q = query.trim().lowercase()
                item.prediction.itemName.lowercase().contains(q) ||
                    item.stageLabel.lowercase().contains(q) ||
                    item.id.lowercase().contains(q) ||
                    item.storageCondition.lowercase().contains(q) ||
                    item.storageConditionLabel.lowercase().contains(q)
            }

            matchesFilter && matchesSearch
        }

        return when (sort) {
            HistorySort.NEWEST -> filtered.sortedByDescending { it.timestamp }
            HistorySort.OLDEST -> filtered.sortedBy { it.timestamp }
            HistorySort.RUL_ASC -> filtered.sortedBy { it.ambientRulDays }
            HistorySort.RUL_DESC -> filtered.sortedByDescending { it.ambientRulDays }
            HistorySort.CONFIDENCE_DESC -> filtered.sortedByDescending { it.prediction.ripeness.confidence }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _controls.update { it.copy(query = query) }
    }

    fun onFilterSelected(filter: HistoryFilter) {
        _controls.update { it.copy(filter = filter) }
    }

    fun onSortSelected(sort: HistorySort) {
        _controls.update { it.copy(sort = sort) }
    }

    fun onResetFilters() {
        _controls.update { it.copy(query = "", filter = HistoryFilter.ALL) }
    }

    fun restoreScan(item: ScanHistoryItemUi) {
        val produceType = com.freshiq.app.domain.model.ProduceType.fromBackendValue(item.prediction.foodType)
        scanSessionManager.setActiveScan(
            prediction = item.prediction,
            imageUri = item.thumbnailPath.ifBlank { null },
            storageCondition = item.storageCondition,
            produceType = produceType
        )
    }

    fun requestDeleteScan(id: String) {
        _controls.update { it.copy(deleteCandidateId = id) }
    }

    fun cancelDeleteScan() {
        _controls.update { it.copy(deleteCandidateId = null) }
    }

    fun confirmDeleteScan() {
        val idToDelete = _controls.value.deleteCandidateId ?: return
        scope.launch {
            try {
                scanHistoryRepository.deleteScan(idToDelete)
                _controls.update { it.copy(deleteCandidateId = null) }
            } catch (e: Exception) {
                _controls.update { it.copy(errorMessage = "Failed to delete scan: ${e.message}") }
            }
        }
    }

    fun requestClearAll() {
        _controls.update { it.copy(showClearAllDialog = true) }
    }

    fun cancelClearAll() {
        _controls.update { it.copy(showClearAllDialog = false) }
    }

    fun confirmClearAll() {
        scope.launch {
            try {
                scanHistoryRepository.clearHistory()
                _controls.update { it.copy(showClearAllDialog = false) }
            } catch (e: Exception) {
                _controls.update { it.copy(errorMessage = "Failed to clear history: ${e.message}") }
            }
        }
    }

    fun dismissError() {
        _controls.update { it.copy(errorMessage = null) }
    }
}
