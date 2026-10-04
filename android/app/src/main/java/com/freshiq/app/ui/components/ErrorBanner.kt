package com.freshiq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCardAlert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTheme
import com.freshiq.app.ui.theme.FreshIQTypography
import com.freshiq.app.ui.theme.StatusErrorBg
import com.freshiq.app.ui.theme.StatusErrorBorder
import com.freshiq.app.ui.theme.StatusErrorIcon
import com.freshiq.app.ui.theme.StatusErrorText
import com.freshiq.app.ui.theme.StatusWarningBg
import com.freshiq.app.ui.theme.StatusWarningBorder
import com.freshiq.app.ui.theme.StatusWarningIcon
import com.freshiq.app.ui.theme.StatusWarningText

enum class FreshIQErrorType {
    BackendUnavailable,
    InvalidFile,
    EmptyFile,
    CorruptedImage,
    OversizedImage,
    GenericPredictionFailure
}

data class ErrorDetails(
    val title: String,
    val message: String,
    val icon: ImageVector,
    val isWarning: Boolean = false
)

fun getErrorDetails(type: FreshIQErrorType, customMessage: String? = null): ErrorDetails {
    return when (type) {
        FreshIQErrorType.BackendUnavailable -> ErrorDetails(
            title = "Backend Connection Failed",
            message = customMessage ?: "Unable to connect to FreshIQ API at 10.0.2.2:8000. Please verify the server is running.",
            icon = Icons.Default.CloudOff
        )
        FreshIQErrorType.InvalidFile -> ErrorDetails(
            title = "Unsupported Format",
            message = customMessage ?: "Please supply a valid produce photo (JPEG, PNG, or WebP).",
            icon = Icons.Default.ImageNotSupported,
            isWarning = true
        )
        FreshIQErrorType.EmptyFile -> ErrorDetails(
            title = "Empty File",
            message = customMessage ?: "Selected image file contains 0 bytes. Please capture or pick another file.",
            icon = Icons.Default.SdCardAlert,
            isWarning = true
        )
        FreshIQErrorType.CorruptedImage -> ErrorDetails(
            title = "Corrupted Image",
            message = customMessage ?: "The image payload could not be decoded. The file may be damaged.",
            icon = Icons.Default.ErrorOutline
        )
        FreshIQErrorType.OversizedImage -> ErrorDetails(
            title = "Image Exceeds Size Limit",
            message = customMessage ?: "Image size exceeds 10MB. Please resize or take a standard resolution photo.",
            icon = Icons.Default.Warning,
            isWarning = true
        )
        FreshIQErrorType.GenericPredictionFailure -> ErrorDetails(
            title = "Inference Pipeline Error",
            message = customMessage ?: "Produce classification failed during tensor extraction. Please retry.",
            icon = Icons.Default.ErrorOutline
        )
    }
}

@Composable
fun ErrorBanner(
    errorType: FreshIQErrorType,
    modifier: Modifier = Modifier,
    customMessage: String? = null,
    onRetry: (() -> Unit)? = null
) {
    val details = getErrorDetails(errorType, customMessage)

    val bgColor = if (details.isWarning) StatusWarningBg else StatusErrorBg
    val borderColor = if (details.isWarning) StatusWarningBorder else StatusErrorBorder
    val textColor = if (details.isWarning) StatusWarningText else StatusErrorText
    val iconColor = if (details.isWarning) StatusWarningIcon else StatusErrorIcon

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(FreshIQRadius.radiusMd))
            .border(1.dp, borderColor, RoundedCornerShape(FreshIQRadius.radiusMd))
            .padding(FreshIQSpacing.spaceLg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = details.icon,
                contentDescription = details.title,
                tint = iconColor,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = details.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = details.message,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = textColor.copy(alpha = 0.9f)
                )

                if (onRetry != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = onRetry,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = textColor
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Retry Analysis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorBannerVariantsPreview() {
    FreshIQTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ErrorBanner(
                errorType = FreshIQErrorType.BackendUnavailable,
                onRetry = {}
            )
            ErrorBanner(
                errorType = FreshIQErrorType.OversizedImage
            )
            ErrorBanner(
                errorType = FreshIQErrorType.GenericPredictionFailure,
                onRetry = {}
            )
        }
    }
}
