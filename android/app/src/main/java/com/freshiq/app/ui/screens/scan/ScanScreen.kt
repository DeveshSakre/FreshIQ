package com.freshiq.app.ui.screens.scan

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.freshiq.app.domain.model.ProduceType
import com.freshiq.app.ui.components.ErrorBanner
import com.freshiq.app.ui.components.FreshCard
import com.freshiq.app.ui.components.FreshCardVariant
import com.freshiq.app.ui.components.FreshPrimaryButton
import com.freshiq.app.ui.components.FreshSecondaryButton
import com.freshiq.app.ui.components.LoadingTensorOverlay
import com.freshiq.app.ui.components.OpticalReticleOverlay
import com.freshiq.app.ui.navigation.Screen
import com.freshiq.app.ui.theme.BgCard
import com.freshiq.app.ui.theme.BgPage
import com.freshiq.app.ui.theme.BgSubtle
import com.freshiq.app.ui.theme.BorderLight
import com.freshiq.app.ui.theme.BorderSubtle
import com.freshiq.app.ui.theme.Forest
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTheme
import com.freshiq.app.ui.theme.FreshIQTypography
import com.freshiq.app.ui.theme.LimeGlow
import com.freshiq.app.ui.theme.PrimaryDark
import com.freshiq.app.ui.theme.PrimaryGreen
import com.freshiq.app.ui.theme.PrimaryLight
import com.freshiq.app.ui.theme.StatusErrorText
import com.freshiq.app.ui.theme.TextMain
import com.freshiq.app.ui.theme.TextMuted
import com.freshiq.app.ui.theme.TextSubtle

@Composable
fun ScanScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Photo picker launcher (Gallery)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onImageSelected(uri)
        }
    }

    // Camera photo capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            viewModel.onCameraPhotoCaptured()
        } else {
            viewModel.onCameraPhotoCancelled()
        }
    }

    val launchCameraDirectly = {
        val uri = viewModel.createCameraPictureUri()
        if (uri != null) {
            try {
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                viewModel.onCameraLaunchError("Unable to launch camera app: ${e.localizedMessage ?: "Unknown error"}")
            }
        } else {
            viewModel.onCameraLaunchError("Unable to create secure storage file for camera capture.")
        }
    }

    // Runtime camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCameraDirectly()
        } else {
            viewModel.onCameraPermissionDenied()
        }
    }

    val onTakePhotoClick = {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCameraPermission) {
            launchCameraDirectly()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    ScanScreenContent(
        uiState = uiState,
        modifier = modifier,
        onSelectProduceType = { viewModel.setProduceType(it) },
        onChooseGallery = {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onTakePhoto = onTakePhotoClick,
        onLoadSample = { stage, name -> viewModel.onLoadSample(stage, name) },
        onRemoveImage = { viewModel.onRemoveImage() },
        onSelectStorageCondition = { cond -> viewModel.setStorageCondition(cond) },
        onSubmit = {
            viewModel.onSubmit {
                navController.navigate(Screen.Analysis.route)
            }
        },
        onRetry = {
            viewModel.onSubmit {
                navController.navigate(Screen.Analysis.route)
            }
        }
    )
}

@Composable
fun ScanScreenContent(
    uiState: ScanUiState,
    modifier: Modifier = Modifier,
    onSelectProduceType: (ProduceType) -> Unit = {},
    onChooseGallery: () -> Unit = {},
    onTakePhoto: () -> Unit = {},
    onLoadSample: (Int, String) -> Unit = { _, _ -> },
    onRemoveImage: () -> Unit = {},
    onSelectStorageCondition: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgPage)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceXl)
        ) {
            // =========================================================================
            // 1. CONTEXTUAL HEADER & STEPPER PILL
            // =========================================================================
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(PrimaryLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryGreen)
                            )
                            Text(
                                text = "Step 1 of 3: Produce Intake",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark,
                                letterSpacing = 0.4.sp
                            )
                        }

                        Text(
                            text = "#FIQ-OPTICAL",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Scan & Estimate Ripeness",
                        style = FreshIQTypography.headlineLarge,
                        color = Forest
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Upload or capture a clear photo of your Hass avocado to determine optical ripening stage, shelf-life envelope, and remaining usable days.",
                        style = FreshIQTypography.bodyMedium,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3-Step Pill Stepper Indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Step 1: Active
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Forest),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "1",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Intake",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Forest
                            )
                        }

                        Text("→", color = TextSubtle, fontSize = 12.sp)

                        // Step 2: Vision AI
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.alpha(0.5f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(BgSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "2",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextMain
                                )
                            }
                            Text(
                                text = "Vision AI",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        Text("→", color = TextSubtle, fontSize = 12.sp)

                        // Step 3: What-If RUL
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.alpha(0.5f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(BgSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "3",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextMain
                                )
                            }
                            Text(
                                text = "What-If",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 2. ERROR BANNER (When errorType != null)
            // =========================================================================
            if (uiState.errorType != null) {
                item {
                    ErrorBanner(
                        errorType = uiState.errorType,
                        customMessage = uiState.customErrorMessage,
                        onRetry = if (uiState.selectedFile != null) onRetry else null
                    )
                }
            }

            // =========================================================================
            // 3. TARGET PRODUCE SELECTOR (Avocado vs Mango)
            // =========================================================================
            item {
                ProduceSelectorCard(
                    selectedProduce = uiState.produceType,
                    onSelectProduce = onSelectProduceType
                )
            }

            // =========================================================================
            // 4. MAIN INTAKE SECTION (Empty vs Staged)
            // =========================================================================
            item {
                if (uiState.selectedFile == null || uiState.selectedImageUri == null) {
                    // EMPTY INTAKE DROPZONE
                    EmptyIntakeDropzone(
                        produceType = uiState.produceType,
                        onChooseGallery = onChooseGallery,
                        onTakePhoto = onTakePhoto,
                        onLoadSample = onLoadSample
                    )
                } else {
                    // STAGED SPECIMEN WITH OPTICAL RETICLE OVERLAY
                    StagedSpecimenCard(
                        uiState = uiState,
                        onRetake = onChooseGallery,
                        onTakePhoto = onTakePhoto,
                        onRemove = onRemoveImage,
                        onSelectStorageCondition = onSelectStorageCondition,
                        onSubmit = onSubmit
                    )
                }
            }

            // =========================================================================
            // 5. PRODUCE TAXONOMY CARD
            // =========================================================================
            item {
                ProduceTaxonomyCard(produceType = uiState.produceType)
            }

            // =========================================================================
            // 6. IMAGE QUALITY CAPTURE GUIDELINES (4 Scientific Rules)
            // =========================================================================
            item {
                ImageQualityGuidelinesCard()
            }

            // Spacer for bottom navigation padding
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // =========================================================================
        // 6. INFERENCE LOADING STATE OVERLAY
        // =========================================================================
        if (uiState.isSubmitting) {
            LoadingTensorOverlay(
                modifier = Modifier.fillMaxSize(),
                title = "Running Produce AI Inference",
                modelArchitecture = "MobileNetV3-Small \u2022 HistGradientBoosting",
                caption = "Extracting epidermal tensors & kinetic decay profile..."
            )
        }
    }
}

// =========================================================================
// Sub-Components
// =========================================================================

@Composable
private fun EmptyIntakeDropzone(
    produceType: ProduceType = ProduceType.AVOCADO,
    onChooseGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onLoadSample: (Int, String) -> Unit
) {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Default
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dashed Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
                    .background(Color(0xA6F1F5F9))
                    .border(
                        width = 2.dp,
                        color = BorderSubtle,
                        shape = RoundedCornerShape(FreshIQRadius.radiusLg)
                    )
                    .padding(vertical = 32.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Glowing upload icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload produce",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Select or capture produce photo",
                        style = FreshIQTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "High-resolution single produce captures for multi-band epidermal texture calibration.",
                        style = FreshIQTypography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action buttons: Gallery & Camera
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        if (maxWidth < 360.dp) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                FreshPrimaryButton(
                                    text = "Choose Image",
                                    icon = Icons.Default.PhotoLibrary,
                                    onClick = onChooseGallery,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                FreshSecondaryButton(
                                    text = "Take Photo",
                                    icon = Icons.Default.CameraAlt,
                                    onClick = onTakePhoto,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FreshPrimaryButton(
                                    text = "Choose Image",
                                    icon = Icons.Default.PhotoLibrary,
                                    onClick = onChooseGallery,
                                    modifier = Modifier.weight(1f)
                                )
                                FreshSecondaryButton(
                                    text = "Take Photo",
                                    icon = Icons.Default.CameraAlt,
                                    onClick = onTakePhoto,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Disabled State Indicator
                    Row(
                        modifier = Modifier
                            .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TextSubtle,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Analyze Produce (Select Image First)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextSubtle
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Supported Formats
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FormatTag("JPG")
                        FormatTag("PNG")
                        FormatTag("WEBP")
                        Text(
                            text = "• Max 10 MB payload",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (produceType == ProduceType.AVOCADO) {
                // Quick Synthetic Calibrated Specimen Selector
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Or test instantly with a synthetic calibrated specimen:",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SamplePillButton(stage = 1, label = "S1 Underripe", onClick = { onLoadSample(1, "Stage 1 Underripe") })
                        SamplePillButton(stage = 2, label = "S2 Breaking", onClick = { onLoadSample(2, "Stage 2 Breaking") })
                        SamplePillButton(stage = 3, label = "S3 Ripe", onClick = { onLoadSample(3, "Stage 3 Ripe") })
                        SamplePillButton(stage = 4, label = "S4 Peak", onClick = { onLoadSample(4, "Stage 4 Peak") })
                        SamplePillButton(stage = 5, label = "S5 Overripe", onClick = { onLoadSample(5, "Stage 5 Overripe") })
                    }
                }
            } else if (produceType == ProduceType.MANGO) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusSm))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Mango intake: capture or select a photo to classify 5-stage ripeness.",
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusSm))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Banana intake: capture or select a photo to classify 3-stage ripeness (Unripe, Semi-ripe, Ripe).",
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun SamplePillButton(stage: Int, label: String, onClick: () -> Unit) {
    Text(
        text = label,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Forest,
        modifier = Modifier
            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
            .background(BgSubtle)
            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    )
}

@Composable
private fun FormatTag(name: String) {
    Text(
        text = name,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Forest,
        modifier = Modifier
            .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    )
}

@Composable
private fun StagedSpecimenCard(
    uiState: ScanUiState,
    onRetake: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemove: () -> Unit,
    onSelectStorageCondition: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val metadata = uiState.fileMetadata

    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Default
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Staged status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Specimen staged",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Staged Produce Specimen",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Forest
                    )
                }

                Text(
                    text = if (uiState.isSubmitting) "ANALYZING TENSORS..." else "READY FOR INFERENCE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.isSubmitting) Color(0xFF92400E) else PrimaryDark,
                    modifier = Modifier
                        .background(
                            color = if (uiState.isSubmitting) Color(0xFFFEF3C7) else PrimaryLight,
                            shape = RoundedCornerShape(FreshIQRadius.radiusFull)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Specimen Image with OpticalReticleOverlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                OpticalReticleOverlay(
                    modifier = Modifier.fillMaxSize(),
                    roiLabel = "ROI 1: Epicarp Surface",
                    telemetryTopRight = "Optical Calibration",
                    telemetryBottomLeft = "Format: ${metadata?.format ?: "JPEG"}",
                    telemetryBottomRight = "Coverage: Centered"
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uiState.selectedImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Staged produce image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata Strip with Retake & Remove
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusMd))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = metadata?.fileName ?: "avocado_specimen.jpg",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Forest,
                        maxLines = 1
                    )
                    Text(
                        text = "${metadata?.sizeMB ?: "0.0"} MB • ${metadata?.width ?: 0} × ${metadata?.height ?: 0} px",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Retake (Gallery)
                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .clickable { onRetake() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retake",
                            tint = Forest,
                            modifier = Modifier.size(13.dp)
                        )
                        Text("Retake", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Forest)
                    }

                    // Remove
                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .clickable { onRemove() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = StatusErrorText,
                            modifier = Modifier.size(13.dp)
                        )
                        Text("Remove", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StatusErrorText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Condition Selector
            Text(
                text = "CURRENT STORAGE ENVIRONMENT:",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Forest,
                letterSpacing = 0.4.sp
            )

            if (uiState.produceType == ProduceType.MANGO) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Informational tracking: Mango ripeness is evaluated purely optically without Arrhenius shelf-life modeling.",
                    fontSize = 10.5.sp,
                    color = TextMuted,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StorageConditionButton(
                        id = "ambient",
                        label = "Ambient Counter (~20°C)",
                        selectedId = uiState.storageCondition,
                        onSelect = onSelectStorageCondition,
                        modifier = Modifier.weight(1f)
                    )
                    StorageConditionButton(
                        id = "20C",
                        label = "Controlled Room (20°C)",
                        selectedId = uiState.storageCondition,
                        onSelect = onSelectStorageCondition,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StorageConditionButton(
                        id = "10C",
                        label = "Cold Storage (10°C)",
                        selectedId = uiState.storageCondition,
                        onSelect = onSelectStorageCondition,
                        modifier = Modifier.weight(1f)
                    )
                    StorageConditionButton(
                        id = "4C",
                        label = "Refrigerator (4°C)",
                        selectedId = uiState.storageCondition,
                        onSelect = onSelectStorageCondition,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Analyze Produce CTA Button
            FreshPrimaryButton(
                text = if (uiState.isSubmitting) {
                    "Extracting Ripening Tensors..."
                } else if (uiState.produceType == ProduceType.MANGO) {
                    "Analyze Mango Ripeness"
                } else {
                    "Analyze Freshness & Shelf-Life"
                },
                icon = Icons.Default.AutoAwesome,
                isLoading = uiState.isSubmitting,
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (uiState.produceType == ProduceType.MANGO) {
                    "Estimated inference latency: ~25ms on MobileNetV3 Mango Classifier"
                } else {
                    "Estimated inference latency: ~25ms on MobileNetV3 + HistGradientBoosting"
                },
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StorageConditionButton(
    id: String,
    label: String,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = id == selectedId
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
            .background(if (isSelected) PrimaryLight else Color.White)
            .border(1.dp, if (isSelected) PrimaryGreen else BorderLight, RoundedCornerShape(FreshIQRadius.radiusSm))
            .clickable { onSelect(id) }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) PrimaryDark else TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun ProduceTaxonomyCard(produceType: ProduceType = ProduceType.AVOCADO) {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Default
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRODUCE TAXONOMY",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (produceType == ProduceType.AVOCADO) "ACTIVE PRODUCTION" else "CLASSIFIER CALIBRATED",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark,
                    modifier = Modifier
                        .background(PrimaryLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusMd))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val badgeBg = when (produceType) {
                    ProduceType.AVOCADO -> Forest
                    ProduceType.MANGO -> Color(0xFFD97706)
                    ProduceType.BANANA -> Color(0xFFCA8A04)
                }
                val badgeCode = when (produceType) {
                    ProduceType.AVOCADO -> "HA"
                    ProduceType.MANGO -> "MG"
                    ProduceType.BANANA -> "BN"
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCode,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = LimeGlow,
                        fontSize = 16.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    val name = when (produceType) {
                        ProduceType.AVOCADO -> "Hass Avocado"
                        ProduceType.MANGO -> "Mango"
                        ProduceType.BANANA -> "Cavendish Banana"
                    }
                    val scientific = when (produceType) {
                        ProduceType.AVOCADO -> "Persea americana cv. Hass"
                        ProduceType.MANGO -> "Mangifera indica"
                        ProduceType.BANANA -> "Musa acuminata"
                    }
                    val detail = when (produceType) {
                        ProduceType.AVOCADO -> "Calibrated for climacteric respiration kinetics, skin melanin pigmentation shifts, and firmness decay."
                        ProduceType.MANGO -> "Calibrated for optical ripening stage classification across 5 developmental phases (Unripe to Perished)."
                        ProduceType.BANANA -> "Calibrated for optical ripening stage classification across 3 developmental phases (Unripe, Semi-ripe, Ripe)."
                    }

                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Forest
                    )
                    Text(
                        text = scientific,
                        fontFamily = FontFamily.Monospace,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = detail,
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val note = when (produceType) {
                ProduceType.AVOCADO -> "Avocado Calibration: Full lifecycle support with optical ripeness, continuous index, and multi-scenario RUL prediction."
                ProduceType.MANGO -> "Mango Calibration: Dedicated 5-stage optical ripeness classification without remaining shelf-life (RUL) modeling."
                ProduceType.BANANA -> "Banana Calibration: Dedicated 3-stage optical ripeness classification without remaining shelf-life (RUL) modeling."
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0FDF4), RoundedCornerShape(FreshIQRadius.radiusSm))
                    .border(1.dp, Color(0xFFDCFCE7), RoundedCornerShape(FreshIQRadius.radiusSm))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = note,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = PrimaryDark
                )
            }
        }
    }
}

@Composable
private fun ProduceSelectorCard(
    selectedProduce: ProduceType,
    onSelectProduce: (ProduceType) -> Unit
) {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Default
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TARGET PRODUCE SPECIMEN",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = if (selectedProduce == ProduceType.AVOCADO) "VISION + RUL" else "CLASSIFICATION ONLY",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedProduce == ProduceType.AVOCADO) PrimaryDark else Color(0xFFB45309),
                    modifier = Modifier
                        .background(
                            if (selectedProduce == ProduceType.AVOCADO) PrimaryLight else Color(0xFFFEF3C7),
                            RoundedCornerShape(FreshIQRadius.radiusFull)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Three produce selector pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusSm))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ProduceTabButton(
                    title = "Avocado",
                    subtitle = "P. americana",
                    isSelected = selectedProduce == ProduceType.AVOCADO,
                    badge = "Full Pipeline",
                    onClick = { onSelectProduce(ProduceType.AVOCADO) },
                    modifier = Modifier.weight(1f)
                )
                ProduceTabButton(
                    title = "Mango",
                    subtitle = "M. indica",
                    isSelected = selectedProduce == ProduceType.MANGO,
                    badge = "5 Stages",
                    onClick = { onSelectProduce(ProduceType.MANGO) },
                    modifier = Modifier.weight(1f)
                )
                ProduceTabButton(
                    title = "Banana",
                    subtitle = "M. acuminata",
                    isSelected = selectedProduce == ProduceType.BANANA,
                    badge = "3 Stages",
                    onClick = { onSelectProduce(ProduceType.BANANA) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val description = when (selectedProduce) {
                ProduceType.AVOCADO -> "Full lifecycle analysis: 5-stage optical ripeness, continuous index, remaining shelf-life (RUL), and multi-temperature storage scenarios."
                ProduceType.MANGO -> "Dedicated 5-stage ripeness classification (Unripe to Perished). Note: Remaining shelf life (RUL) and storage temperature scenarios are not available."
                ProduceType.BANANA -> "Dedicated 3-stage ripeness classification (Unripe, Semi-ripe, Ripe). Note: Remaining shelf life (RUL) and storage temperature scenarios are not available."
            }

            Text(
                text = description,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun ProduceTabButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    badge: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
            .background(if (isSelected) Color.White else Color.Transparent)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) PrimaryGreen else Color.Transparent,
                shape = RoundedCornerShape(FreshIQRadius.radiusSm)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (isSelected) Forest else TextMuted
                )
                Text(
                    text = badge,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) PrimaryDark else TextSubtle,
                    modifier = Modifier
                        .background(
                            if (isSelected) PrimaryLight else Color.Transparent,
                            RoundedCornerShape(FreshIQRadius.radiusFull)
                        )
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
            Text(
                text = subtitle,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSubtle
            )
        }
    }
}

@Composable
private fun ImageQualityGuidelinesCard() {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Default
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Image Quality Guidelines",
                style = FreshIQTypography.headlineSmall,
                color = Forest
            )
            Text(
                text = "Follow these 4 capture rules to maximize MobileNetV3 feature extraction precision:",
                style = FreshIQTypography.bodySmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GuidelineRuleRow(
                    ruleNumber = "1",
                    title = "Natural, Diffuse Lighting",
                    description = "Avoid harsh camera flash bursts or pitch-black shadows that wash out epidermal chlorophyll tone."
                )
                GuidelineRuleRow(
                    ruleNumber = "2",
                    title = "50% to 70% Frame Fill",
                    description = "Position the avocado centrally so pebble density and skin texture occupy the primary field of view."
                )
                GuidelineRuleRow(
                    ruleNumber = "3",
                    title = "Clean, Uniform Countertop",
                    description = "Place produce on a cutting board, marble, or clean table. Remove busy extraneous clutter."
                )
                GuidelineRuleRow(
                    ruleNumber = "4",
                    title = "No Mesh Bags or Cling Film",
                    description = "Polyethylene wrap and mesh packaging cause optical refraction artifacts on the CNN feature layers."
                )
            }
        }
    }
}

@Composable
private fun GuidelineRuleRow(
    ruleNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusSm))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Forest),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ruleNumber,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = Forest
            )
            Text(
                text = description,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = TextMuted
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenEmptyPreview() {
    FreshIQTheme {
        ScanScreenContent(uiState = ScanUiState())
    }
}
