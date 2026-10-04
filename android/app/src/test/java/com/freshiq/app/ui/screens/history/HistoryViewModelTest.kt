package com.freshiq.app.ui.screens.history

import com.freshiq.app.data.local.ScanHistoryEntity
import com.freshiq.app.data.model.PredictionResponseDto
import com.freshiq.app.data.model.RipenessAssessmentDto
import com.freshiq.app.data.model.ScenarioRulDto
import com.freshiq.app.data.model.StageProbabilitiesDto
import com.freshiq.app.data.repository.ScanHistoryRepository
import com.freshiq.app.data.repository.ScanSessionManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeScanHistoryRepository : ScanHistoryRepository {
    private val _scans = MutableStateFlow<List<ScanHistoryEntity>>(emptyList())

    override fun getScanHistory(): Flow<List<ScanHistoryEntity>> = _scans

    override suspend fun getScanById(id: String): ScanHistoryEntity? {
        return _scans.value.find { it.id == id }
    }

    override suspend fun saveScan(
        id: String,
        imagePath: String,
        storageCondition: String,
        prediction: PredictionResponseDto
    ) {
        val gson = Gson()
        val entity = ScanHistoryEntity(
            id = id,
            timestamp = System.currentTimeMillis(),
            thumbnailPath = imagePath,
            storageCondition = storageCondition,
            predictionJson = gson.toJson(prediction)
        )
        _scans.value = listOf(entity) + _scans.value
    }

    override suspend fun deleteScan(id: String) {
        _scans.value = _scans.value.filterNot { it.id == id }
    }

    override suspend fun clearHistory() {
        _scans.value = emptyList()
    }

    fun setScans(scans: List<ScanHistoryEntity>) {
        _scans.value = scans
    }
}

class HistoryViewModelTest {

    private val gson = Gson()

    private fun createSamplePrediction(
        stage: Int,
        stageLabel: String,
        confidence: Float,
        ambientRul: Float,
        refrigGain: Float = 0f
    ): PredictionResponseDto {
        return PredictionResponseDto(
            itemName = "Avocado (Hass)",
            ripeness = RipenessAssessmentDto(
                predictedRipeningStage = stage,
                stageLabel = stageLabel,
                confidence = confidence,
                expectedContinuousRipeningStage = stage.toFloat(),
                probabilities = StageProbabilitiesDto(0f, 0f, 0f, 0f, 0f)
            ),
            scenarios = mapOf(
                "ambient" to ScenarioRulDto(
                    condition = "ambient",
                    temperatureC = 20f,
                    estimatedRulDays = ambientRul,
                    isExtrapolated = false,
                    method = "arrhenius_kinetics",
                    uncertaintyNote = null,
                    disclaimer = "Test disclaimer",
                    recommendation = "Test recommendation"
                )
            ),
            refrigerationExtensionGainDays = refrigGain,
            actionableRecommendation = "Test recommendation",
            legalDisclaimer = "Test disclaimer"
        )
    }

    @Test
    fun testEmptyStateInitially() = runBlocking {
        val repository = FakeScanHistoryRepository()
        val sessionManager = ScanSessionManager()
        val testScope = CoroutineScope(Dispatchers.Unconfined)
        val viewModel = HistoryViewModel(repository, sessionManager, gson, testScope)

        val state = viewModel.uiState.first { !it.isLoading }
        assertTrue(state.rawScans.isEmpty())
        assertTrue(state.filteredScans.isEmpty())
        assertEquals(0, state.telemetry.total)
        assertEquals("0.00", state.telemetry.wasteSaved)
        assertEquals("0.0", state.telemetry.co2Avoided)
    }

    @Test
    fun testTelemetryAndCategorization() = runBlocking {
        val repository = FakeScanHistoryRepository()
        val sessionManager = ScanSessionManager()
        val testScope = CoroutineScope(Dispatchers.Unconfined)

        val predStage1 = createSamplePrediction(stage = 1, stageLabel = "Underripe", confidence = 0.95f, ambientRul = 7.0f, refrigGain = 4.5f)
        val predStage4 = createSamplePrediction(stage = 4, stageLabel = "Ripe Second Stage", confidence = 0.88f, ambientRul = 1.5f, refrigGain = 0f)
        val predStage5 = createSamplePrediction(stage = 5, stageLabel = "Overripe", confidence = 0.99f, ambientRul = 0.0f, refrigGain = 0f)

        val entities = listOf(
            ScanHistoryEntity(id = "scan_001", timestamp = 1000L, thumbnailPath = "/tmp/1.jpg", storageCondition = "ambient", predictionJson = gson.toJson(predStage1)),
            ScanHistoryEntity(id = "scan_002", timestamp = 2000L, thumbnailPath = "/tmp/2.jpg", storageCondition = "refrigerator", predictionJson = gson.toJson(predStage4)),
            ScanHistoryEntity(id = "scan_003", timestamp = 3000L, thumbnailPath = "/tmp/3.jpg", storageCondition = "ambient", predictionJson = gson.toJson(predStage5))
        )
        repository.setScans(entities)

        val viewModel = HistoryViewModel(repository, sessionManager, gson, testScope)
        val state = viewModel.uiState.first { it.rawScans.size == 3 }

        assertEquals(3, state.telemetry.total)
        assertEquals(1, state.telemetry.fresh) // Stage 1
        assertEquals(1, state.telemetry.urgent) // Stage 4 / rul <= 2.0
        assertEquals(1, state.telemetry.overripe) // Stage 5
        assertEquals(1, state.telemetry.chilled) // predStage1 has refrigGain > 0
        assertEquals("9.75", state.telemetry.wasteSaved) // 3 * 3.25 = 9.75
        assertEquals("1.4", state.telemetry.co2Avoided) // 3 * 0.45 = 1.35 -> 1.4
    }

    @Test
    fun testFilterChips() = runBlocking {
        val repository = FakeScanHistoryRepository()
        val sessionManager = ScanSessionManager()
        val testScope = CoroutineScope(Dispatchers.Unconfined)

        val predStage1 = createSamplePrediction(stage = 1, stageLabel = "Underripe", confidence = 0.95f, ambientRul = 7.0f, refrigGain = 3.0f)
        val predStage4 = createSamplePrediction(stage = 4, stageLabel = "Ripe Second Stage", confidence = 0.88f, ambientRul = 1.5f, refrigGain = 0f)
        val predStage5 = createSamplePrediction(stage = 5, stageLabel = "Overripe", confidence = 0.99f, ambientRul = 0.0f, refrigGain = 0f)

        repository.setScans(listOf(
            ScanHistoryEntity(id = "scan_001", timestamp = 1000L, thumbnailPath = "", storageCondition = "ambient", predictionJson = gson.toJson(predStage1)),
            ScanHistoryEntity(id = "scan_002", timestamp = 2000L, thumbnailPath = "", storageCondition = "ambient", predictionJson = gson.toJson(predStage4)),
            ScanHistoryEntity(id = "scan_003", timestamp = 3000L, thumbnailPath = "", storageCondition = "ambient", predictionJson = gson.toJson(predStage5))
        ))

        val viewModel = HistoryViewModel(repository, sessionManager, gson, testScope)
        viewModel.uiState.first { it.rawScans.size == 3 }

        viewModel.onFilterSelected(HistoryFilter.URGENT)
        val urgentState = viewModel.uiState.first { it.selectedFilter == HistoryFilter.URGENT }
        assertEquals(1, urgentState.filteredScans.size)
        assertEquals("scan_002", urgentState.filteredScans[0].id)

        viewModel.onFilterSelected(HistoryFilter.OVERRIPE)
        val overripeState = viewModel.uiState.first { it.selectedFilter == HistoryFilter.OVERRIPE }
        assertEquals(1, overripeState.filteredScans.size)
        assertEquals("scan_003", overripeState.filteredScans[0].id)

        viewModel.onFilterSelected(HistoryFilter.CHILLED)
        val chilledState = viewModel.uiState.first { it.selectedFilter == HistoryFilter.CHILLED }
        assertEquals(1, chilledState.filteredScans.size)
        assertEquals("scan_001", chilledState.filteredScans[0].id)

        viewModel.onFilterSelected(HistoryFilter.FRESH)
        val freshState = viewModel.uiState.first { it.selectedFilter == HistoryFilter.FRESH }
        assertEquals(1, freshState.filteredScans.size)
        assertEquals("scan_001", freshState.filteredScans[0].id)

        viewModel.onResetFilters()
        val allState = viewModel.uiState.first { it.selectedFilter == HistoryFilter.ALL }
        assertEquals(3, allState.filteredScans.size)
    }

    @Test
    fun testSearchFiltering() = runBlocking {
        val repository = FakeScanHistoryRepository()
        val sessionManager = ScanSessionManager()
        val testScope = CoroutineScope(Dispatchers.Unconfined)

        val pred1 = createSamplePrediction(stage = 2, stageLabel = "Breaking", confidence = 0.90f, ambientRul = 4.0f)
        val pred2 = createSamplePrediction(stage = 3, stageLabel = "Firm Ripe", confidence = 0.85f, ambientRul = 3.0f)

        repository.setScans(listOf(
            ScanHistoryEntity(id = "scan_batch_alpha", timestamp = 1000L, thumbnailPath = "", storageCondition = "cold_10c", predictionJson = gson.toJson(pred1)),
            ScanHistoryEntity(id = "scan_batch_beta", timestamp = 2000L, thumbnailPath = "", storageCondition = "ambient", predictionJson = gson.toJson(pred2))
        ))

        val viewModel = HistoryViewModel(repository, sessionManager, gson, testScope)
        viewModel.uiState.first { it.rawScans.size == 2 }

        viewModel.onSearchQueryChanged("alpha")
        val searchState = viewModel.uiState.first { it.searchQuery == "alpha" }
        assertEquals(1, searchState.filteredScans.size)
        assertEquals("scan_batch_alpha", searchState.filteredScans[0].id)

        viewModel.onSearchQueryChanged("firm ripe")
        val searchState2 = viewModel.uiState.first { it.searchQuery == "firm ripe" }
        assertEquals(1, searchState2.filteredScans.size)
        assertEquals("scan_batch_beta", searchState2.filteredScans[0].id)
    }

    @Test
    fun testRestorationIntoScanSessionManager() = runBlocking {
        val repository = FakeScanHistoryRepository()
        val sessionManager = ScanSessionManager()
        val testScope = CoroutineScope(Dispatchers.Unconfined)

        val pred = createSamplePrediction(stage = 3, stageLabel = "Firm Ripe", confidence = 0.92f, ambientRul = 3.5f)
        val entity = ScanHistoryEntity(
            id = "historical_scan_42",
            timestamp = 5000L,
            thumbnailPath = "/local/path/img.jpg",
            storageCondition = "controlled_20c",
            predictionJson = gson.toJson(pred)
        )
        repository.setScans(listOf(entity))

        val viewModel = HistoryViewModel(repository, sessionManager, gson, testScope)
        val state = viewModel.uiState.first { it.rawScans.size == 1 }
        val item = state.rawScans[0]

        // Initially sessionManager has no active scan
        assertNull(sessionManager.activePrediction.value)

        // Restore scan
        viewModel.restoreScan(item)

        // Verify restored session
        assertNotNull(sessionManager.activePrediction.value)
        assertEquals(3, sessionManager.activePrediction.value?.ripeness?.predictedRipeningStage)
        assertEquals("Firm Ripe", sessionManager.activePrediction.value?.ripeness?.stageLabel)
        assertEquals("/local/path/img.jpg", sessionManager.activeImageUri.value)
        assertEquals("controlled_20c", sessionManager.activeStorageCondition.value)
    }

    @Test
    fun testDeletionAndClearAll() = runBlocking {
        val repository = FakeScanHistoryRepository()
        val sessionManager = ScanSessionManager()
        val testScope = CoroutineScope(Dispatchers.Unconfined)

        val pred = createSamplePrediction(stage = 3, stageLabel = "Firm Ripe", confidence = 0.92f, ambientRul = 3.5f)
        repository.setScans(listOf(
            ScanHistoryEntity(id = "item_1", timestamp = 1000L, thumbnailPath = "", storageCondition = "ambient", predictionJson = gson.toJson(pred)),
            ScanHistoryEntity(id = "item_2", timestamp = 2000L, thumbnailPath = "", storageCondition = "ambient", predictionJson = gson.toJson(pred))
        ))

        val viewModel = HistoryViewModel(repository, sessionManager, gson, testScope)
        viewModel.uiState.first { it.rawScans.size == 2 }

        // Test single delete flow
        viewModel.requestDeleteScan("item_1")
        val delState = viewModel.uiState.first { it.deleteCandidateId == "item_1" }
        assertEquals("item_1", delState.deleteCandidateId)

        viewModel.confirmDeleteScan()
        val afterDelState = viewModel.uiState.first { it.rawScans.size == 1 }
        assertEquals("item_2", afterDelState.rawScans[0].id)
        assertNull(afterDelState.deleteCandidateId)

        // Test clear all flow
        viewModel.requestClearAll()
        val clearPromptState = viewModel.uiState.first { it.showClearAllDialog }
        assertTrue(clearPromptState.showClearAllDialog)

        viewModel.confirmClearAll()
        val emptyState = viewModel.uiState.first { it.rawScans.isEmpty() }
        assertFalse(emptyState.showClearAllDialog)
        assertTrue(emptyState.rawScans.isEmpty())
    }

    private fun createSampleMangoPrediction(
        stage: Int,
        stageLabel: String,
        confidence: Float
    ): PredictionResponseDto {
        return PredictionResponseDto(
            foodType = "mango",
            itemName = "Mango (White Chaunsa Late)",
            modelId = "FreshIQ_MobileNetV3_Mango",
            ripeness = RipenessAssessmentDto(
                predictedRipeningStage = stage,
                stageLabel = stageLabel,
                confidence = confidence,
                expectedContinuousRipeningStage = null,
                probabilities = StageProbabilitiesDto(
                    unripe = if (stage == 0) confidence else 0.05f,
                    semiripe = if (stage == 1) confidence else 0.05f,
                    fullyRipe = if (stage == 2) confidence else 0.05f,
                    overripe = if (stage == 3) confidence else 0.05f,
                    perished = if (stage == 4) confidence else 0.05f
                )
            ),
            rulAvailable = false,
            scenarios = null,
            refrigerationExtensionGainDays = null,
            actionableRecommendation = "Enjoy mango fresh.",
            legalDisclaimer = "Legal test disclaimer"
        )
    }

    @Test
    fun testMangoScanParsing() = runBlocking {
        val repository = FakeScanHistoryRepository()
        val sessionManager = ScanSessionManager()
        val testScope = CoroutineScope(Dispatchers.Unconfined)

        val mangoPred = createSampleMangoPrediction(stage = 2, stageLabel = "Fully Ripe", confidence = 0.94f)
        val entity = ScanHistoryEntity(
            id = "mango_scan_1",
            timestamp = 1000L,
            thumbnailPath = "/path/mango.jpg",
            storageCondition = "ambient",
            predictionJson = gson.toJson(mangoPred)
        )
        repository.setScans(listOf(entity))

        val viewModel = HistoryViewModel(repository, sessionManager, gson, testScope)
        val state = viewModel.uiState.first { it.rawScans.isNotEmpty() }

        val item = state.rawScans[0]
        assertEquals("mango_scan_1", item.id)
        assertEquals(2, item.stage)
        assertEquals("Fully Ripe", item.stageLabel)
        assertEquals(0.0f, item.ambientRulDays, 0.001f)
        assertFalse(item.hasRefrigerationGain)
        assertFalse(item.isTerminal)
        assertFalse(item.isUrgent)
        assertEquals("Fully Ripe (Peak Eating)", item.statusBadgeText)
    }

    @Test
    fun testMangoPerishedScanParsing() = runBlocking {
        val repository = FakeScanHistoryRepository()
        val sessionManager = ScanSessionManager()
        val testScope = CoroutineScope(Dispatchers.Unconfined)

        val mangoPred = createSampleMangoPrediction(stage = 4, stageLabel = "Perished", confidence = 0.98f)
        val entity = ScanHistoryEntity(
            id = "mango_scan_perished",
            timestamp = 1000L,
            thumbnailPath = "/path/mango_perished.jpg",
            storageCondition = "ambient",
            predictionJson = gson.toJson(mangoPred)
        )
        repository.setScans(listOf(entity))

        val viewModel = HistoryViewModel(repository, sessionManager, gson, testScope)
        val state = viewModel.uiState.first { it.rawScans.isNotEmpty() }

        val item = state.rawScans[0]
        assertEquals(4, item.stage)
        assertEquals(0.0f, item.ambientRulDays, 0.001f)
        assertTrue(item.isTerminal)
        assertEquals("Perished (Discard)", item.statusBadgeText)
    }

    @Test
    fun testMangoRestoreScanSetsProduceType() = runBlocking {
        val repository = FakeScanHistoryRepository()
        val sessionManager = ScanSessionManager()
        val testScope = CoroutineScope(Dispatchers.Unconfined)

        val mangoPred = createSampleMangoPrediction(stage = 1, stageLabel = "Semiripe", confidence = 0.88f)
        val entity = ScanHistoryEntity(
            id = "mango_scan_restore",
            timestamp = 1000L,
            thumbnailPath = "/path/mango.jpg",
            storageCondition = "ambient",
            predictionJson = gson.toJson(mangoPred)
        )
        repository.setScans(listOf(entity))

        val viewModel = HistoryViewModel(repository, sessionManager, gson, testScope)
        val state = viewModel.uiState.first { it.rawScans.isNotEmpty() }
        val item = state.rawScans[0]

        viewModel.restoreScan(item)

        assertEquals(com.freshiq.app.domain.model.ProduceType.MANGO, sessionManager.activeProduceType.value)
        assertEquals("mango", sessionManager.activePrediction.value?.foodType)
        assertFalse(sessionManager.activePrediction.value?.rulAvailable ?: true)
    }

    private fun createSampleBananaPrediction(
        stage: Int,
        stageLabel: String,
        confidence: Float
    ): PredictionResponseDto {
        return PredictionResponseDto(
            itemName = "Banana (Cavendish)",
            foodType = "banana",
            ripeness = RipenessAssessmentDto(
                predictedRipeningStage = stage,
                stageLabel = stageLabel,
                confidence = confidence,
                expectedContinuousRipeningStage = null,
                probabilities = StageProbabilitiesDto(
                    unripe = if (stage == 0) confidence else 0.05f,
                    semiRipeBanana = if (stage == 1) confidence else 0.05f,
                    ripeBanana = if (stage == 2) confidence else 0.05f
                )
            ),
            rulAvailable = false,
            scenarios = null,
            refrigerationExtensionGainDays = null,
            actionableRecommendation = "Store bananas at room temperature.",
            legalDisclaimer = "Legal banana disclaimer"
        )
    }

    @Test
    fun testBananaScanParsingAllStages() = runBlocking {
        val repository = FakeScanHistoryRepository()
        val sessionManager = ScanSessionManager()
        val testScope = CoroutineScope(Dispatchers.Unconfined)

        val stages = listOf(
            Triple(0, "Unripe", "Unripe (Storage/Holding)"),
            Triple(1, "Semi-ripe", "Semi-ripe (Ripening)"),
            Triple(2, "Ripe", "Ripe (Ready to Eat)")
        )

        val entities = stages.mapIndexed { idx, (stage, label, _) ->
            val bananaPred = createSampleBananaPrediction(stage = stage, stageLabel = label, confidence = 0.92f)
            ScanHistoryEntity(
                id = "banana_scan_$idx",
                timestamp = (idx + 1) * 1000L,
                thumbnailPath = "/path/banana_$idx.jpg",
                storageCondition = "ambient",
                predictionJson = gson.toJson(bananaPred)
            )
        }
        repository.setScans(entities)

        val viewModel = HistoryViewModel(repository, sessionManager, gson, testScope)
        val state = viewModel.uiState.first { it.rawScans.size == 3 }

        stages.forEachIndexed { idx, (expectedStage, expectedLabel, expectedBadge) ->
            val item = state.rawScans.find { it.id == "banana_scan_$idx" }
            assertNotNull(item)
            assertEquals(expectedStage, item!!.stage)
            assertEquals(expectedLabel, item.stageLabel)
            assertEquals(0.0f, item.ambientRulDays, 0.001f)
            assertFalse(item.hasRefrigerationGain)
            assertFalse(item.isTerminal)
            assertFalse(item.isUrgent)
            assertEquals(expectedBadge, item.statusBadgeText)
        }
    }

    @Test
    fun testBananaRestoreScanSetsProduceType() = runBlocking {
        val repository = FakeScanHistoryRepository()
        val sessionManager = ScanSessionManager()
        val testScope = CoroutineScope(Dispatchers.Unconfined)

        val bananaPred = createSampleBananaPrediction(stage = 2, stageLabel = "Ripe", confidence = 0.96f)
        val entity = ScanHistoryEntity(
            id = "banana_scan_restore",
            timestamp = 1000L,
            thumbnailPath = "/path/banana.jpg",
            storageCondition = "ambient",
            predictionJson = gson.toJson(bananaPred)
        )
        repository.setScans(listOf(entity))

        val viewModel = HistoryViewModel(repository, sessionManager, gson, testScope)
        val state = viewModel.uiState.first { it.rawScans.isNotEmpty() }
        val item = state.rawScans[0]

        viewModel.restoreScan(item)

        assertEquals(com.freshiq.app.domain.model.ProduceType.BANANA, sessionManager.activeProduceType.value)
        assertEquals("banana", sessionManager.activePrediction.value?.foodType)
        assertFalse(sessionManager.activePrediction.value?.rulAvailable ?: true)
    }
}
