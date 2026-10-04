package com.freshiq.app.ui.screens.scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshiq.app.data.repository.FreshIQRepository
import com.freshiq.app.data.repository.ScanSessionManager
import com.freshiq.app.ui.components.FreshIQErrorType
import com.freshiq.app.domain.model.ProduceType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

data class FileMetadata(
    val fileName: String,
    val sizeBytes: Long,
    val sizeMB: String,
    val width: Int,
    val height: Int,
    val format: String
)

data class ScanUiState(
    val produceType: ProduceType = ProduceType.AVOCADO,
    val selectedImageUri: Uri? = null,
    val selectedFile: File? = null,
    val fileMetadata: FileMetadata? = null,
    val storageCondition: String = "ambient",
    val isSubmitting: Boolean = false,
    val errorType: FreshIQErrorType? = null,
    val customErrorMessage: String? = null
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: FreshIQRepository,
    private val scanSessionManager: ScanSessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private var currentCameraTempFile: File? = null
    private var currentCameraContentUri: Uri? = null

    fun createCameraPictureUri(): Uri? {
        return try {
            val cacheDir = context.cacheDir
            val tempFile = File.createTempFile("camera_scan_", ".jpg", cacheDir)
            currentCameraTempFile = tempFile
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            currentCameraContentUri = uri
            uri
        } catch (e: Exception) {
            Log.e("ScanViewModel", "Failed to create camera picture URI", e)
            null
        }
    }

    fun onCameraPhotoCaptured() {
        val file = currentCameraTempFile
        val contentUri = currentCameraContentUri
        if (file != null && file.exists() && file.length() > 0L) {
            val uriToUse = contentUri ?: try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: Exception) {
                Uri.fromFile(file)
            }
            validateAndStageFile(file, uriToUse, file.name)
        } else {
            Log.w("ScanViewModel", "Camera photo capture returned without valid image file")
        }
    }

    fun onCameraPhotoCancelled() {
        val file = currentCameraTempFile
        if (file != null && file.exists() && file.length() == 0L) {
            try {
                file.delete()
            } catch (e: Exception) {
                Log.w("ScanViewModel", "Failed to delete empty temporary camera file: ${e.message}")
            }
        }
    }

    fun onCameraPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            errorType = FreshIQErrorType.InvalidFile,
            customErrorMessage = "Camera permission is required to capture photos. Please grant camera access in system settings or choose an image from the gallery."
        )
    }

    fun onCameraLaunchError(message: String) {
        _uiState.value = _uiState.value.copy(
            errorType = FreshIQErrorType.InvalidFile,
            customErrorMessage = message
        )
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            try {
                val tempFile = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val targetFile = File(context.cacheDir, "picked_scan_${UUID.randomUUID()}.jpg")
                    inputStream?.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    targetFile
                }
                validateAndStageFile(tempFile, uri, "produce_capture.jpg")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorType = FreshIQErrorType.CorruptedImage,
                    customErrorMessage = "Could not read selected image: ${e.message}"
                )
            }
        }
    }

    fun onLoadSample(stage: Int, stageName: String) {
        _uiState.value = _uiState.value.copy(produceType = ProduceType.AVOCADO)
        viewModelScope.launch {
            val sampleFile = withContext(Dispatchers.IO) {
                generateSyntheticAvocadoTexture(stage, stageName)
            }
            if (sampleFile != null && sampleFile.exists()) {
                val uri = Uri.fromFile(sampleFile)
                validateAndStageFile(sampleFile, uri, sampleFile.name)
            }
        }
    }

    private fun validateAndStageFile(file: File, uri: Uri, originalName: String) {
        // 1. Non-empty check
        if (!file.exists() || file.length() == 0L) {
            _uiState.value = _uiState.value.copy(
                errorType = FreshIQErrorType.EmptyFile,
                customErrorMessage = "The selected file is empty (0 bytes). Please choose a valid photograph."
            )
            return
        }

        // 2. Max size 10 MB check
        val maxSizeBytes = 10 * 1024 * 1024L
        if (file.length() > maxSizeBytes) {
            val sizeMB = String.format("%.1f", file.length() / (1024f * 1024f))
            _uiState.value = _uiState.value.copy(
                errorType = FreshIQErrorType.OversizedImage,
                customErrorMessage = "Selected file is $sizeMB MB. Maximum allowed image payload is 10.0 MB."
            )
            return
        }

        // 3. Image decodability and bounds
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            _uiState.value = _uiState.value.copy(
                errorType = FreshIQErrorType.CorruptedImage,
                customErrorMessage = "Unsupported or corrupted image file. Please provide a standard JPEG, PNG, or WebP photo."
            )
            return
        }

        val format = when (options.outMimeType?.lowercase()) {
            "image/png" -> "PNG"
            "image/webp" -> "WEBP"
            else -> "JPEG"
        }

        val sizeMBFormatted = String.format("%.2f", file.length() / (1024f * 1024f))

        val metadata = FileMetadata(
            fileName = originalName,
            sizeBytes = file.length(),
            sizeMB = sizeMBFormatted,
            width = options.outWidth,
            height = options.outHeight,
            format = format
        )

        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            selectedFile = file,
            fileMetadata = metadata,
            errorType = null,
            customErrorMessage = null
        )
    }

    fun setProduceType(produceType: ProduceType) {
        if (_uiState.value.produceType != produceType) {
            _uiState.value = _uiState.value.copy(
                produceType = produceType,
                errorType = null,
                customErrorMessage = null
            )
        }
    }

    fun setStorageCondition(condition: String) {
        _uiState.value = _uiState.value.copy(storageCondition = condition)
    }

    fun onRemoveImage() {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = null,
            selectedFile = null,
            fileMetadata = null,
            errorType = null,
            customErrorMessage = null
        )
    }

    fun onSubmit(onNavigateToAnalysis: () -> Unit) {
        val file = _uiState.value.selectedFile ?: return
        if (_uiState.value.isSubmitting) return

        _uiState.value = _uiState.value.copy(
            isSubmitting = true,
            errorType = null,
            customErrorMessage = null
        )

        viewModelScope.launch {
            val currentProduce = _uiState.value.produceType
            val result = repository.predictProduce(
                imageFile = file,
                storageCondition = _uiState.value.storageCondition,
                foodType = currentProduce.backendValue
            )
            result.fold(
                onSuccess = { predictionDto ->
                    scanSessionManager.setActiveScan(
                        prediction = predictionDto,
                        imageUri = _uiState.value.selectedImageUri?.toString(),
                        storageCondition = _uiState.value.storageCondition,
                        produceType = currentProduce
                    )
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    onNavigateToAnalysis()
                },
                onFailure = { error ->
                    val errorMsg = error.message ?: ""
                    val errorType = when {
                        errorMsg.contains("413") -> FreshIQErrorType.OversizedImage
                        errorMsg.contains("400") -> FreshIQErrorType.CorruptedImage
                        errorMsg.contains("422") -> FreshIQErrorType.InvalidFile
                        errorMsg.contains("503") ||
                        errorMsg.contains("connect", ignoreCase = true) ||
                        errorMsg.contains("timeout", ignoreCase = true) ||
                        errorMsg.contains("unable to resolve", ignoreCase = true) -> FreshIQErrorType.BackendUnavailable
                        else -> FreshIQErrorType.GenericPredictionFailure
                    }

                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorType = errorType,
                        customErrorMessage = if (errorType == FreshIQErrorType.BackendUnavailable) {
                            "Unable to connect to FreshIQ API at ${com.freshiq.app.BuildConfig.BASE_URL}. Please verify that the FastAPI backend server is running and USB adb reverse is active."
                        } else {
                            error.message
                        }
                    )
                }
            )
        }
    }

    private fun generateSyntheticAvocadoTexture(stage: Int, stageName: String): File? {
        return try {
            val width = 400
            val height = 400
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val (c1, c2, c3) = when (stage) {
                1 -> Triple(0xFF166534.toInt(), 0xFF15803D.toInt(), 0xFF14532D.toInt()) // Stage 1 Bright Green
                2 -> Triple(0xFF3F6212.toInt(), 0xFF4D7C0F.toInt(), 0xFF1A3826.toInt()) // Stage 2 Breaking
                3 -> Triple(0xFF422006.toInt(), 0xFF2A1810.toInt(), 0xFF1C1917.toInt()) // Stage 3 Darkening Ripe
                4 -> Triple(0xFF1C1917.toInt(), 0xFF171412.toInt(), 0xFF0C0A09.toInt()) // Stage 4 Peak Dark
                else -> Triple(0xFF0C0A09.toInt(), 0xFF080706.toInt(), 0xFF030202.toInt()) // Stage 5 Overripe
            }

            val shader = RadialGradient(
                200f, 200f, 190f,
                intArrayOf(c1, c2, c3),
                floatArrayOf(0f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
            val bgPaint = Paint().apply { this.shader = shader }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Texture pebbles
            val dotPaintWhite = Paint().apply { color = 0x1AFFFFFF }
            val dotPaintDark = Paint().apply { color = 0x33000000 }
            val random = Random(42 + stage)
            for (i in 0..500) {
                val rx = random.nextFloat() * width
                val ry = random.nextFloat() * height
                val radius = random.nextFloat() * 3f + 1f
                val paint = if (random.nextBoolean()) dotPaintWhite else dotPaintDark
                canvas.drawCircle(rx, ry, radius, paint)
            }

            val file = File(context.cacheDir, "sample_avocado_${stageName.lowercase().replace(" ", "_")}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            bitmap.recycle()
            file
        } catch (e: Exception) {
            null
        }
    }
}
