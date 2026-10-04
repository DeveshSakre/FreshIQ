package com.freshiq.app.ui.screens.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshiq.app.data.local.ScanHistoryEntity
import com.freshiq.app.data.model.PredictionResponseDto
import com.freshiq.app.data.repository.FreshIQRepository
import com.freshiq.app.data.repository.ScanHistoryRepository
import com.freshiq.app.data.repository.ScanSessionManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val scanSessionManager: ScanSessionManager,
    private val scanHistoryRepository: ScanHistoryRepository,
    private val freshIQRepository: FreshIQRepository,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val activePrediction: StateFlow<PredictionResponseDto?> = scanSessionManager.activePrediction
    val activeImageUri: StateFlow<String?> = scanSessionManager.activeImageUri
    val activeStorageCondition: StateFlow<String> = scanSessionManager.activeStorageCondition
    val activeProduceType: StateFlow<com.freshiq.app.domain.model.ProduceType> = scanSessionManager.activeProduceType

    val recentScans: StateFlow<List<ScanHistoryEntity>> = scanHistoryRepository.getScanHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSavedToHistory = MutableStateFlow(false)
    val isSavedToHistory: StateFlow<Boolean> = _isSavedToHistory.asStateFlow()

    private val _isGeneratingDemo = MutableStateFlow(false)
    val isGeneratingDemo: StateFlow<Boolean> = _isGeneratingDemo.asStateFlow()

    private val _demoError = MutableStateFlow<String?>(null)
    val demoError: StateFlow<String?> = _demoError.asStateFlow()

    fun saveToHistory() {
        val prediction = activePrediction.value ?: return
        val imageUri = activeImageUri.value ?: ""
        val storageCondition = activeStorageCondition.value

        viewModelScope.launch {
            try {
                val scanId = "scan_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
                scanHistoryRepository.saveScan(
                    id = scanId,
                    imagePath = imageUri,
                    storageCondition = storageCondition,
                    prediction = prediction
                )
                _isSavedToHistory.value = true
                delay(2500)
                _isSavedToHistory.value = false
            } catch (e: Exception) {
                // If saving fails, keep status unchanged
            }
        }
    }

    fun loadHistoricalScan(entity: ScanHistoryEntity) {
        try {
            val prediction = gson.fromJson(entity.predictionJson, PredictionResponseDto::class.java)
            val produceType = com.freshiq.app.domain.model.ProduceType.fromBackendValue(prediction.foodType)
            scanSessionManager.setActiveScan(
                prediction = prediction,
                imageUri = entity.thumbnailPath.ifBlank { null },
                storageCondition = entity.storageCondition,
                produceType = produceType
            )
        } catch (e: Exception) {
            _demoError.value = "Failed to load scan: ${e.message}"
        }
    }

    fun runDemoScan(sampleStage: Int = 3) {
        if (_isGeneratingDemo.value) return
        _isGeneratingDemo.value = true
        _demoError.value = null

        viewModelScope.launch {
            try {
                val demoFile = withContext(Dispatchers.IO) {
                    generateSyntheticAvocadoImage(sampleStage)
                }
                val result = freshIQRepository.predictProduce(
                    imageFile = demoFile,
                    storageCondition = "ambient"
                )
                result.fold(
                    onSuccess = { prediction ->
                        val uri = Uri.fromFile(demoFile).toString()
                        scanSessionManager.setActiveScan(
                            prediction = prediction,
                            imageUri = uri,
                            storageCondition = "ambient"
                        )
                        _isGeneratingDemo.value = false
                    },
                    onFailure = { error ->
                        _demoError.value = "Demo prediction failed: ${error.message ?: "FastAPI server unreachable"}"
                        _isGeneratingDemo.value = false
                    }
                )
            } catch (e: Exception) {
                _demoError.value = "Failed to create demo: ${e.message}"
                _isGeneratingDemo.value = false
            }
        }
    }

    private fun generateSyntheticAvocadoImage(stage: Int): File {
        val size = 480
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Palette matching Stage 3 Firm Ripe
        val outerColor = android.graphics.Color.rgb(45, 53, 40)
        val midColor = android.graphics.Color.rgb(63, 98, 18)
        val innerColor = android.graphics.Color.rgb(85, 107, 47)

        val bgPaint = Paint().apply {
            color = android.graphics.Color.rgb(240, 245, 241)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

        val avocadoPaint = Paint().apply {
            isAntiAlias = true
            shader = RadialGradient(
                size * 0.48f,
                size * 0.45f,
                size * 0.42f,
                intArrayOf(innerColor, midColor, outerColor),
                floatArrayOf(0f, 0.65f, 1f),
                Shader.TileMode.CLAMP
            )
        }

        val centerX = size / 2f
        val centerY = size / 2f
        canvas.save()
        canvas.scale(0.85f, 1.15f, centerX, centerY)
        canvas.drawCircle(centerX, centerY, size * 0.38f, avocadoPaint)
        canvas.restore()

        val texturePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val random = Random(42)
        for (i in 0 until 500) {
            val angle = random.nextDouble(0.0, Math.PI * 2)
            val dist = random.nextDouble(0.0, (size * 0.35).toDouble())
            val px = centerX + (dist * kotlin.math.cos(angle)).toFloat()
            val py = centerY + ((dist * 1.25) * kotlin.math.sin(angle)).toFloat()
            val pebbleRadius = random.nextFloat() * 3.5f + 1.2f
            val alpha = random.nextInt(40, 140)

            texturePaint.color = android.graphics.Color.argb(alpha, 18, 32, 18)
            canvas.drawCircle(px, py, pebbleRadius, texturePaint)
        }

        val cacheDir = context.cacheDir
        val demoFile = File(cacheDir, "demo_avocado_${System.currentTimeMillis()}.jpg")
        FileOutputStream(demoFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        bitmap.recycle()
        return demoFile
    }
}
