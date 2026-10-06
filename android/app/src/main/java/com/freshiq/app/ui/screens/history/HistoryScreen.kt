package com.freshiq.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.freshiq.app.ui.components.FoodSafetyDisclaimer
import com.freshiq.app.ui.components.FreshCard
import com.freshiq.app.ui.components.FreshCardVariant
import com.freshiq.app.ui.components.FreshPrimaryButton
import com.freshiq.app.ui.components.FreshSecondaryButton
import com.freshiq.app.ui.components.RipenessBadge
import com.freshiq.app.ui.navigation.Screen
import com.freshiq.app.ui.theme.BotanicalError
import com.freshiq.app.ui.theme.BotanicalErrorContainer
import com.freshiq.app.ui.theme.BotanicalOnPrimary
import com.freshiq.app.ui.theme.BotanicalOnSurface
import com.freshiq.app.ui.theme.BotanicalOnSurfaceVariant
import com.freshiq.app.ui.theme.BotanicalOutline
import com.freshiq.app.ui.theme.BotanicalPrimary
import com.freshiq.app.ui.theme.BotanicalPrimaryContainer
import com.freshiq.app.ui.theme.BotanicalPrimaryFixed
import com.freshiq.app.ui.theme.BotanicalSurface
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerHigh
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLow
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLowest
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTypography
import java.util.Locale

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BotanicalSurface)
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = BotanicalPrimary,
                    strokeWidth = 3.dp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = FreshIQSpacing.margin),
                verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.gutter)
            ) {
                item {
                    Spacer(modifier = Modifier.height(FreshIQSpacing.spaceSm))
                }

                // 1. Header & Scans Count
                item {
                    HistoryHeaderSection(
                        totalScans = uiState.rawScans.size,
                        hasScans = uiState.rawScans.isNotEmpty(),
                        onClearAll = { viewModel.requestClearAll() }
                    )
                }

                // 2. Search & Filter Bar (Only when there are recorded scans)
                if (uiState.rawScans.isNotEmpty()) {
                    item {
                        HistoryFilterAndSearchSection(
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                            selectedFilter = uiState.selectedFilter,
                            onFilterSelected = { viewModel.onFilterSelected(it) }
                        )
                    }
                }

                // 3. Scan List or Empty States
                if (uiState.rawScans.isEmpty()) {
                    item {
                        HistoryEmptyState(
                            onScanProduce = { navController.navigate(Screen.Scan.route) },
                            onBrowseInsights = { navController.navigate(Screen.Insights.route) }
                        )
                    }
                } else if (uiState.filteredScans.isEmpty()) {
                    item {
                        HistoryNoMatchesState(
                            onResetFilters = { viewModel.onResetFilters() }
                        )
                    }
                } else {
                    items(
                        items = uiState.filteredScans,
                        key = { it.id }
                    ) { item ->
                        HistoryScanCard(
                            item = item,
                            onClick = {
                                viewModel.restoreScan(item)
                                navController.navigate(Screen.Analysis.route)
                            },
                            onDelete = { viewModel.requestDeleteScan(item.id) }
                        )
                    }
                }

                // 4. Food Safety Disclaimer at Bottom
                item {
                    FoodSafetyDisclaimer(
                        modifier = Modifier.fillMaxWidth(),
                        backendDisclaimer = "History records are preserved locally in Room database. RUL projections reflect validated model estimates, not biological guarantees."
                    )
                    Spacer(modifier = Modifier.height(FreshIQSpacing.spaceLg))
                }
            }
        }

        // Dialog: Confirm Single Scan Deletion
        if (uiState.deleteCandidateId != null) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelDeleteScan() },
                title = {
                    Text(
                        text = "Delete Scan Record?",
                        style = FreshIQTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BotanicalOnSurface
                    )
                },
                text = {
                    Text(
                        text = "This action removes this scan entry from your local history database. This cannot be undone.",
                        style = FreshIQTypography.bodyMedium,
                        color = BotanicalOnSurfaceVariant
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmDeleteScan() }) {
                        Text(
                            text = "Delete",
                            color = BotanicalError,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDeleteScan() }) {
                        Text(
                            text = "Cancel",
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                },
                containerColor = BotanicalSurfaceContainerLowest,
                shape = RoundedCornerShape(FreshIQRadius.radiusLg)
            )
        }

        // Dialog: Confirm Clear All History
        if (uiState.showClearAllDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelClearAll() },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BotanicalErrorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = BotanicalError,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Clear All History?",
                            style = FreshIQTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BotanicalOnSurface
                        )
                    }
                },
                text = {
                    Text(
                        text = "Are you sure you want to permanently erase all saved scan evaluations from this device?",
                        style = FreshIQTypography.bodyMedium,
                        color = BotanicalOnSurfaceVariant
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmClearAll() }) {
                        Text(
                            text = "Clear All",
                            color = BotanicalError,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelClearAll() }) {
                        Text(
                            text = "Keep Records",
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                },
                containerColor = BotanicalSurfaceContainerLowest,
                shape = RoundedCornerShape(FreshIQRadius.radiusLg)
            )
        }
    }
}

// =========================================================================
// Header Section: Scan History Title & Summary
// =========================================================================

@Composable
private fun HistoryHeaderSection(
    totalScans: Int,
    hasScans: Boolean,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Scan History",
                    style = FreshIQTypography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BotanicalOnSurface
                )

                if (hasScans) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(BotanicalPrimaryFixed.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$totalScans",
                            fontFamily = FontFamily.Monospace,
                            style = FreshIQTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BotanicalPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Locally preserved evaluation records on your device",
                style = FreshIQTypography.bodySmall,
                color = BotanicalOnSurfaceVariant
            )
        }

        if (hasScans) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .background(BotanicalSurfaceContainerLow)
                    .clickable { onClearAll() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear All",
                        tint = BotanicalOutline,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Clear",
                        style = FreshIQTypography.labelSmall,
                        color = BotanicalOutline
                    )
                }
            }
        }
    }
}

// =========================================================================
// Filter & Search Controls
// =========================================================================

@Composable
private fun HistoryFilterAndSearchSection(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedFilter: HistoryFilter,
    onFilterSelected: (HistoryFilter) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search Input Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
                .background(BotanicalSurfaceContainerLowest)
                .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(FreshIQRadius.radiusMd))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = BotanicalOutline,
                    modifier = Modifier.size(18.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search by produce, stage, or scan id...",
                            style = FreshIQTypography.bodyMedium,
                            color = BotanicalOutline
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = 14.sp,
                            color = BotanicalOnSurface
                        ),
                        cursorBrush = SolidColor(BotanicalPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = BotanicalOutline,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onSearchQueryChanged("") }
                    )
                }
            }
        }

        // Horizontal Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HistoryFilter.values().forEach { filter ->
                val isSelected = selectedFilter == filter
                val bg = if (isSelected) BotanicalPrimary else BotanicalSurfaceContainerLow
                val textColor = if (isSelected) BotanicalOnPrimary else BotanicalOnSurfaceVariant
                val borderCol = if (isSelected) BotanicalPrimary else Color(0xFFE6E6DF)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(bg)
                        .border(1.dp, borderCol, RoundedCornerShape(FreshIQRadius.radiusFull))
                        .clickable { onFilterSelected(filter) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter.label,
                        style = FreshIQTypography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

// =========================================================================
// History Scan Card: Presentation-Only
// =========================================================================

@Composable
private fun HistoryScanCard(
    item: ScanHistoryItemUi,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isAvocado = item.prediction.foodType.equals("avocado", ignoreCase = true)

    FreshCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        variant = FreshCardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FreshIQSpacing.spaceMd)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceMd)
            ) {
                // Thumbnail preview
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
                        .background(BotanicalSurfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.thumbnailPath.isNotBlank()) {
                        AsyncImage(
                            model = item.thumbnailPath,
                            contentDescription = item.prediction.itemName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = BotanicalOutline,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Info Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.prediction.itemName,
                            style = FreshIQTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BotanicalOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Text(
                            text = item.formattedDate,
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            color = BotanicalOutline
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RipenessBadge(stage = item.stage, customLabel = item.stageLabel)

                        Text(
                            text = "${item.confidencePercent}%",
                            fontFamily = FontFamily.Monospace,
                            style = FreshIQTypography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = BotanicalPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.storageConditionLabel,
                            style = FreshIQTypography.bodySmall,
                            color = BotanicalOnSurfaceVariant
                        )

                        // Avocado RUL (ONLY rendered for Avocado when stored)
                        if (isAvocado && item.ambientRulDays > 0f && !item.isTerminal) {
                            Text(
                                text = "~${String.format(Locale.US, "%.1f", item.ambientRulDays)}d RUL",
                                fontFamily = FontFamily.Monospace,
                                style = FreshIQTypography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BotanicalPrimary
                            )
                        }
                    }
                }
            }

            // Stored Recommendation Preview (if available)
            if (item.prediction.actionableRecommendation.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                        .background(BotanicalSurfaceContainerLow)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = item.prediction.actionableRecommendation,
                        style = FreshIQTypography.bodySmall.copy(fontSize = 11.5.sp),
                        color = BotanicalOnSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${item.id.take(8)}",
                    fontFamily = FontFamily.Monospace,
                    style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                    color = BotanicalOutline
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete scan",
                        tint = BotanicalOutline,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onDelete() }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "View Result",
                            style = FreshIQTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BotanicalPrimary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = BotanicalPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// Empty States
// =========================================================================

@Composable
private fun HistoryEmptyState(
    onScanProduce: () -> Unit,
    onBrowseInsights: () -> Unit
) {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BotanicalPrimaryFixed.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = BotanicalPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your scan history will appear here.",
                style = FreshIQTypography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BotanicalOnSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Previous produce evaluations, ripening classifications, and storage recommendations will be saved locally on your device for fast review.",
                style = FreshIQTypography.bodyMedium,
                color = BotanicalOnSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
            ) {
                FreshPrimaryButton(
                    text = "Scan Produce Now",
                    icon = Icons.Default.CameraAlt,
                    onClick = onScanProduce,
                    modifier = Modifier.fillMaxWidth()
                )

                FreshSecondaryButton(
                    text = "Browse Botanical Insights",
                    icon = Icons.Default.AutoAwesome,
                    onClick = onBrowseInsights,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun HistoryNoMatchesState(
    onResetFilters: () -> Unit
) {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Subtle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = BotanicalOutline,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "No matching scans found",
                style = FreshIQTypography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BotanicalOnSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Try adjusting your search keywords or filter selection.",
                style = FreshIQTypography.bodySmall,
                color = BotanicalOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            FreshSecondaryButton(
                text = "Reset Search Filters",
                onClick = onResetFilters
            )
        }
    }
}
