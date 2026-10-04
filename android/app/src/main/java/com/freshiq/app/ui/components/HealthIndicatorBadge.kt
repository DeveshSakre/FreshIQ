package com.freshiq.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshiq.app.data.repository.FreshIQRepository
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQTheme
import com.freshiq.app.ui.theme.PrimaryDark
import com.freshiq.app.ui.theme.PrimaryGreen
import com.freshiq.app.ui.theme.PrimaryLight
import com.freshiq.app.ui.theme.StatusErrorBg
import com.freshiq.app.ui.theme.StatusErrorIcon
import com.freshiq.app.ui.theme.StatusErrorText
import com.freshiq.app.ui.theme.StatusWarningBg
import com.freshiq.app.ui.theme.StatusWarningIcon
import com.freshiq.app.ui.theme.StatusWarningText
import com.freshiq.app.ui.theme.TextMuted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HealthUiState {
    object Loading : HealthUiState
    data class Healthy(val checkpointVerified: Boolean = true) : HealthUiState
    data class Degraded(val message: String = "API Degraded") : HealthUiState
    data class Offline(val message: String = "Backend Offline") : HealthUiState
}

@HiltViewModel
class HealthIndicatorViewModel @Inject constructor(
    private val repository: FreshIQRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HealthUiState>(HealthUiState.Loading)
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    init {
        checkHealth()
    }

    fun checkHealth() {
        viewModelScope.launch {
            _uiState.value = HealthUiState.Loading
            try {
                val result = repository.getHealth()
                result.fold(
                    onSuccess = { dto ->
                        when (dto.status.lowercase()) {
                            "healthy" -> _uiState.value = HealthUiState.Healthy(dto.visionCheckpointVerified)
                            "degraded" -> _uiState.value = HealthUiState.Degraded()
                            else -> _uiState.value = HealthUiState.Offline()
                        }
                    },
                    onFailure = {
                        _uiState.value = HealthUiState.Offline()
                    }
                )
            } catch (e: Exception) {
                _uiState.value = HealthUiState.Offline()
            }
        }
    }
}

@Composable
fun HealthIndicatorBadge(
    modifier: Modifier = Modifier,
    viewModel: HealthIndicatorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    HealthIndicatorBadge(
        state = state,
        onRefresh = { viewModel.checkHealth() },
        modifier = modifier
    )
}

@Composable
fun HealthIndicatorBadge(
    state: HealthUiState,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "health_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    when (state) {
        is HealthUiState.Loading -> {
            Row(
                modifier = modifier
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(FreshIQRadius.radiusFull))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable { onRefresh() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Checking API",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(13.dp)
                        .alpha(pulseAlpha)
                )
                Text(
                    text = "Checking API...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
            }
        }
        is HealthUiState.Healthy -> {
            Row(
                modifier = modifier
                    .background(PrimaryLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable { onRefresh() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .alpha(pulseAlpha)
                        .clip(CircleShape)
                        .background(PrimaryGreen)
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "API Live",
                    tint = PrimaryDark,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "API Live",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
            }
        }
        is HealthUiState.Degraded -> {
            Row(
                modifier = modifier
                    .background(StatusWarningBg, RoundedCornerShape(FreshIQRadius.radiusFull))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable { onRefresh() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "API Degraded",
                    tint = StatusWarningIcon,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "API Degraded",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusWarningText
                )
            }
        }
        is HealthUiState.Offline -> {
            Row(
                modifier = modifier
                    .background(StatusErrorBg, RoundedCornerShape(FreshIQRadius.radiusFull))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable { onRefresh() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "Backend Offline",
                    tint = StatusErrorIcon,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Backend Offline",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusErrorText
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HealthIndicatorBadgeAllStatesPreview() {
    FreshIQTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HealthIndicatorBadge(state = HealthUiState.Healthy())
            HealthIndicatorBadge(state = HealthUiState.Loading)
            HealthIndicatorBadge(state = HealthUiState.Degraded())
            HealthIndicatorBadge(state = HealthUiState.Offline())
        }
    }
}
