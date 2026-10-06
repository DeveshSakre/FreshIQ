package com.freshiq.app.ui.screens.scan

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.freshiq.app.domain.model.ProduceType
import com.freshiq.app.ui.components.ProduceSelectorGrid
import com.freshiq.app.ui.navigation.Screen
import com.freshiq.app.ui.theme.BotanicalError
import com.freshiq.app.ui.theme.BotanicalOnPrimary
import com.freshiq.app.ui.theme.BotanicalOnPrimaryContainer
import com.freshiq.app.ui.theme.BotanicalOnSecondaryFixedVariant
import com.freshiq.app.ui.theme.BotanicalOnSurface
import com.freshiq.app.ui.theme.BotanicalOnSurfaceVariant
import com.freshiq.app.ui.theme.BotanicalOutline
import com.freshiq.app.ui.theme.BotanicalPrimary
import com.freshiq.app.ui.theme.BotanicalPrimaryContainer
import com.freshiq.app.ui.theme.BotanicalPrimaryFixed
import com.freshiq.app.ui.theme.BotanicalPrimaryFixedDim
import com.freshiq.app.ui.theme.BotanicalSecondary
import com.freshiq.app.ui.theme.BotanicalSecondaryContainer
import com.freshiq.app.ui.theme.BotanicalSecondaryFixed
import com.freshiq.app.ui.theme.BotanicalSurface
import com.freshiq.app.ui.theme.BotanicalSurfaceContainer
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerHigh
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerHighest
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLow
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLowest
import com.freshiq.app.ui.theme.BotanicalTertiary
import com.freshiq.app.ui.theme.BotanicalTertiaryFixed
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTypography

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
            .background(BotanicalSurface)
    ) {
        when {
            // State A: Submitting / Analyzing Loading State
            uiState.isSubmitting -> {
                ScanAnalyzingLoadingState(uiState = uiState)
            }

            // State B: Scan Error State
            uiState.errorType != null -> {
                ScanErrorState(
                    uiState = uiState,
                    onTryAgain = onRetry,
                    onChooseDifferent = onChooseGallery,
                    onDismiss = onRemoveImage
                )
            }

            // State C: Image Staged / Preview State
            uiState.selectedImageUri != null -> {
                ScanImagePreviewState(
                    uiState = uiState,
                    onChangeProduce = onRemoveImage,
                    onRetake = onTakePhoto,
                    onAnalyze = onSubmit,
                    onDiscard = onRemoveImage
                )
            }

            // State D: Default Viewfinder & Target Selection State
            else -> {
                ScanCaptureViewfinderState(
                    uiState = uiState,
                    onSelectProduce = onSelectProduceType,
                    onOpenCamera = onTakePhoto,
                    onUploadGallery = onChooseGallery
                )
            }
        }
    }
}

// =========================================================================
// 1. DEFAULT CAPTURE / VIEWFINDER STATE
// =========================================================================
@Composable
private fun ScanCaptureViewfinderState(
    uiState: ScanUiState,
    onSelectProduce: (ProduceType) -> Unit,
    onOpenCamera: () -> Unit,
    onUploadGallery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = FreshIQSpacing.margin, vertical = FreshIQSpacing.spaceSm),
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceMd)
    ) {
        // Target Produce Selector Grid (Phase 2 component)
        ProduceSelectorGrid(
            selectedProduce = uiState.produceType,
            onSelectProduce = onSelectProduce
        )

        // Produce Taxonomy & Capability Header Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
                .background(BotanicalSurfaceContainerLowest)
                .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(FreshIQRadius.radiusLg))
                .padding(FreshIQSpacing.spaceMd)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = uiState.produceType.displayName,
                            style = FreshIQTypography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = BotanicalOnSurface
                        )
                        Text(
                            text = uiState.produceType.scientificName,
                            style = FreshIQTypography.bodySmall,
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BotanicalPrimaryFixed.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Taxonomy",
                            tint = BotanicalPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Capability Tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stageTag = when (uiState.produceType) {
                        ProduceType.BANANA -> "3-Stage Ripeness"
                        else -> "5-Stage Ripeness"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(BotanicalSurfaceContainerLow)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = stageTag,
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            color = BotanicalOnSurfaceVariant
                        )
                    }

                    if (uiState.produceType.hasRulSupport) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(BotanicalSurfaceContainerLow)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Shelf-Life RUL",
                                style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                                color = BotanicalPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(BotanicalSurfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "v2.4 CV Model",
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            color = BotanicalOutline
                        )
                    }
                }
            }
        }

        // Viewfinder Capture Graphic Zone
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(FreshIQRadius.radius2xl))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E211E), Color(0xFF131513))
                    )
                )
                .padding(FreshIQSpacing.spaceLg),
            contentAlignment = Alignment.Center
        ) {
            // Viewfinder Reticle Corners
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.dp,
                        color = BotanicalPrimaryFixed.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(FreshIQRadius.radiusXl)
                    )
            )

            // Center Silhouette
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(BotanicalPrimaryFixed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = "Target Silhouette",
                        tint = BotanicalPrimaryFixed.copy(alpha = 0.6f),
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            // Top Overlay Lighting Pill
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(Color.Black.copy(alpha = 0.60f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Lighting",
                        tint = BotanicalSecondaryFixed,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Even natural daylight",
                        style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White
                    )
                }
            }

            // Bottom Frame Guidance Text
            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Place whole ${uiState.produceType.displayName.lowercase()} within the frame",
                    style = FreshIQTypography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.90f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Center produce body and stem base",
                    style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White.copy(alpha = 0.60f)
                )
            }
        }

        // Primary Action Triggers
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Open Camera Button (Primary 52px Full Pill)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(BotanicalPrimaryContainer)
                    .clickable { onOpenCamera() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Camera",
                        tint = BotanicalOnPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Open Camera",
                        style = FreshIQTypography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = BotanicalOnPrimary
                    )
                }
            }

            // Upload from Gallery Button (Secondary 50px Full Pill)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(BotanicalSurfaceContainerLowest)
                    .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(9999.dp))
                    .clickable { onUploadGallery() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = BotanicalOnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Upload from Gallery",
                        style = FreshIQTypography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = BotanicalOnSurface
                    )
                }
            }
        }

        // Scientific Scan Tip Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
                .background(BotanicalSurfaceContainerLow)
                .padding(FreshIQSpacing.spaceMd)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BotanicalSurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TipsAndUpdates,
                        contentDescription = "Tip",
                        tint = BotanicalPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Scientific Scan Tip",
                        style = FreshIQTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BotanicalOnSurface
                    )
                    Text(
                        text = "Ensure both stem and body surface are visible for optimal accuracy.",
                        style = FreshIQTypography.bodySmall,
                        color = BotanicalOnSurfaceVariant
                    )
                }
            }
        }
    }
}

// =========================================================================
// 2. SCAN IMAGE PREVIEW STATE
// =========================================================================
@Composable
private fun ScanImagePreviewState(
    uiState: ScanUiState,
    onChangeProduce: () -> Unit,
    onRetake: () -> Unit,
    onAnalyze: () -> Unit,
    onDiscard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = FreshIQSpacing.margin, vertical = FreshIQSpacing.spaceSm),
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceMd)
    ) {
        // Target Produce Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BotanicalPrimaryContainer.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = "Target",
                        tint = BotanicalPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "TARGET PRODUCE",
                        style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                        color = BotanicalOnSurfaceVariant,
                        letterSpacing = 0.05.sp
                    )
                    Text(
                        text = uiState.produceType.displayName,
                        style = FreshIQTypography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BotanicalOnSurface
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(BotanicalSurfaceContainer)
                    .clickable { onChangeProduce() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Change",
                        style = FreshIQTypography.labelSmall,
                        color = BotanicalOnSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Change",
                        tint = BotanicalOnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Captured Photo Container with Overlays
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(FreshIQRadius.radius2xl))
                .background(BotanicalSurfaceContainer)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uiState.selectedImageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Captured produce image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top Overlay: Lighting Detected Badge + Retake Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(BotanicalSurfaceContainerLowest.copy(alpha = 0.90f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(BotanicalPrimaryContainer)
                        )
                        Text(
                            text = "Good Lighting Detected",
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            color = BotanicalOnSurface
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(BotanicalSurfaceContainerLowest.copy(alpha = 0.90f))
                        .clickable { onRetake() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Retake",
                            tint = BotanicalOnSurface,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Retake",
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = BotanicalOnSurface
                        )
                    }
                }
            }

            // Bottom Overlay: Image Resolution Metadata Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val width = uiState.fileMetadata?.width ?: 1024
                    val height = uiState.fileMetadata?.height ?: 1024
                    val format = uiState.fileMetadata?.format ?: "RGB Standard"

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color.Black.copy(alpha = 0.50f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$width × $height px",
                            style = FreshIQTypography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color.Black.copy(alpha = 0.50f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = format,
                            style = FreshIQTypography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Ready",
                    tint = BotanicalPrimaryFixed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Ready for AI Analysis Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
                .background(BotanicalSurfaceContainerLow)
                .padding(FreshIQSpacing.spaceMd)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BotanicalPrimaryContainer.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Ready",
                            tint = BotanicalPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Ready for AI Analysis",
                            style = FreshIQTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = BotanicalOnSurface
                        )
                        Text(
                            text = "Image verified for optical color analysis",
                            style = FreshIQTypography.labelSmall,
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(BotanicalPrimaryFixed)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Optimal Scan",
                        style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = BotanicalPrimary
                    )
                }
            }
        }

        // Action Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Prominent Analyze Produce CTA (56px Full Pill with Sparkle Icon)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(BotanicalPrimaryContainer)
                    .clickable { onAnalyze() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Analyze",
                        tint = BotanicalOnPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Analyze Produce",
                        style = FreshIQTypography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = BotanicalOnPrimary
                    )
                }
            }

            // Discard Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDiscard() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Discard & Select Different Photo",
                    style = FreshIQTypography.titleSmall,
                    color = BotanicalOnSurfaceVariant
                )
            }
        }
    }
}

// =========================================================================
// 3. ANALYZING / LOADING STATE
// =========================================================================
@Composable
private fun ScanAnalyzingLoadingState(uiState: ScanUiState) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = FreshIQSpacing.margin, vertical = FreshIQSpacing.spaceLg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceLg)
    ) {
        // Target produce badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9999.dp))
                    .background(BotanicalSurfaceContainerLowest)
                    .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(9999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = BotanicalPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = uiState.produceType.displayName,
                        style = FreshIQTypography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = BotanicalOnSurface
                    )
                }
            }

            Text(
                text = "FreshIQ Optical",
                style = FreshIQTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = BotanicalPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Circular Pulsing Visual Scanner with Produce Thumbnail
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer pulse ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(BotanicalPrimaryFixed.copy(alpha = pulseAlpha * 0.4f))
            )

            // Inner container
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(BotanicalSurfaceContainerLowest)
                    .border(2.dp, BotanicalPrimaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.selectedImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(uiState.selectedImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Analyzing thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = BotanicalPrimaryContainer,
                        modifier = Modifier.size(48.dp)
                    )
                }

                CircularProgressIndicator(
                    color = BotanicalPrimaryContainer,
                    strokeWidth = 3.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Headline & Subtitle
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Analyzing your produce...",
                style = FreshIQTypography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BotanicalOnSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Examining skin pigmentation, surface contours, and optical ripeness cues.",
                style = FreshIQTypography.bodySmall,
                color = BotanicalOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Progression Steps Checklist Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
                .background(BotanicalSurfaceContainerLowest)
                .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(FreshIQRadius.radiusLg))
                .padding(FreshIQSpacing.spaceMd)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Step 1: Completed
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(BotanicalPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Surface illumination calibrated",
                            style = FreshIQTypography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = BotanicalOnSurface
                        )
                        Text(
                            text = "Optimal diffuse ambient lighting detected",
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                }

                // Step 2: In Progress
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(BotanicalPrimaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BotanicalPrimaryContainer)
                        )
                    }
                    Column {
                        Text(
                            text = "Spectral colorimetry in progress...",
                            style = FreshIQTypography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = BotanicalPrimaryContainer
                        )
                        Text(
                            text = "Measuring epidermal pigment degradation",
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                }

                // Step 3: Pending
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(BotanicalSurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(BotanicalOutline)
                        )
                    }
                    Column {
                        Text(
                            text = "Synthesizing ripeness assessment",
                            style = FreshIQTypography.bodyMedium,
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                }
            }
        }

        // Botanical AI Model Info Card
        val modelFamilyName = when (uiState.produceType) {
            ProduceType.AVOCADO -> "Lauraceae Subtropical V4.2"
            ProduceType.MANGO -> "Mangifera Indica V2.0"
            ProduceType.BANANA -> "Musa Acuminata V2.0"
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
                .background(BotanicalSurfaceContainerLow)
                .padding(FreshIQSpacing.spaceMd)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BotanicalSurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Model",
                            tint = BotanicalPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "BOTANICAL AI MODEL",
                            style = FreshIQTypography.labelSmall.copy(fontSize = 9.sp),
                            color = BotanicalOnSurfaceVariant,
                            letterSpacing = 0.05.sp
                        )
                        Text(
                            text = modelFamilyName,
                            style = FreshIQTypography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = BotanicalOnSurface
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(BotanicalSurfaceContainerLowest)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "High Precision",
                        style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                        color = BotanicalTertiary
                    )
                }
            }
        }

        Text(
            text = "Keep camera still while optical scan finishes",
            style = FreshIQTypography.labelSmall,
            color = BotanicalOnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// =========================================================================
// 4. SCAN ERROR STATE
// =========================================================================
@Composable
private fun ScanErrorState(
    uiState: ScanUiState,
    onTryAgain: () -> Unit,
    onChooseDifferent: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = FreshIQSpacing.margin, vertical = FreshIQSpacing.spaceLg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceLg)
    ) {
        // Error Pulse Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(BotanicalSecondaryFixed),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CenterFocusWeak,
                contentDescription = "Scan Error",
                tint = BotanicalSecondary,
                modifier = Modifier.size(36.dp)
            )
        }

        // Headline & Human-readable Explanation
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "We couldn’t analyze this image",
                style = FreshIQTypography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BotanicalOnSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = uiState.customErrorMessage
                    ?: "The produce wasn’t clearly visible or the lighting was too dim for our optical model to accurately determine ripeness.",
                style = FreshIQTypography.bodySmall,
                color = BotanicalOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Constructive Tips Checklist Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
                .background(BotanicalSurfaceContainerLow)
                .padding(FreshIQSpacing.spaceMd)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = BotanicalPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tips for a crisp scan",
                        style = FreshIQTypography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BotanicalOnSurface
                    )
                }

                // Tip 1: Center produce
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(BotanicalSurfaceContainerLowest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CropFree,
                            contentDescription = null,
                            tint = BotanicalPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Center the produce fully",
                            style = FreshIQTypography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = BotanicalOnSurface
                        )
                        Text(
                            text = "Keep the whole fruit comfortably within the viewfinder frame",
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                }

                // Tip 2: Avoid glare
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(BotanicalSurfaceContainerLowest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = null,
                            tint = BotanicalSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Avoid harsh glare and deep shadows",
                            style = FreshIQTypography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = BotanicalOnSurface
                        )
                        Text(
                            text = "Natural daylight captures skin pigments best",
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                }
            }
        }

        // Actions
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Try Again Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(BotanicalPrimaryContainer)
                    .clickable { onTryAgain() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Try Again",
                        tint = BotanicalOnPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Try Again",
                        style = FreshIQTypography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = BotanicalOnPrimary
                    )
                }
            }

            // Choose Different Photo Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(BotanicalSurfaceContainerLowest)
                    .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(9999.dp))
                    .clickable { onChooseDifferent() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Choose Photo",
                        tint = BotanicalOnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Choose Different Photo",
                        style = FreshIQTypography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = BotanicalOnSurface
                    )
                }
            }

            // Dismiss Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss() }
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Dismiss",
                    style = FreshIQTypography.bodySmall,
                    color = BotanicalOnSurfaceVariant
                )
            }
        }
    }
}
