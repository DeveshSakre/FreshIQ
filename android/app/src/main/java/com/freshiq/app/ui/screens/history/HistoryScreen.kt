package com.freshiq.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.freshiq.app.ui.components.BadgeSize
import com.freshiq.app.ui.components.FoodSafetyDisclaimer
import com.freshiq.app.ui.components.FreshCard
import com.freshiq.app.ui.components.FreshPrimaryButton
import com.freshiq.app.ui.components.FreshSecondaryButton
import com.freshiq.app.ui.components.RipenessBadge
import com.freshiq.app.ui.navigation.Screen
import com.freshiq.app.ui.theme.BgCard
import com.freshiq.app.ui.theme.BgPage
import com.freshiq.app.ui.theme.BgSubtle
import com.freshiq.app.ui.theme.BorderLight
import com.freshiq.app.ui.theme.BorderSubtle
import com.freshiq.app.ui.theme.Forest
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.LimeGlow
import com.freshiq.app.ui.theme.PrimaryDark
import com.freshiq.app.ui.theme.PrimaryGreen
import com.freshiq.app.ui.theme.PrimaryLight
import com.freshiq.app.ui.theme.Stage1Fill
import com.freshiq.app.ui.theme.Stage2Fill
import com.freshiq.app.ui.theme.Stage3Fill
import com.freshiq.app.ui.theme.Stage4Fill
import com.freshiq.app.ui.theme.Stage5Fill
import com.freshiq.app.ui.theme.TextMain
import com.freshiq.app.ui.theme.TextMuted
import com.freshiq.app.ui.theme.TextSubtle
import java.util.Locale

private val STAGE_GAUGE_COLORS = mapOf(
    1 to Stage1Fill,
    2 to Stage2Fill,
    3 to Stage3Fill,
    4 to Stage4Fill,
    5 to Stage5Fill
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPage)
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryGreen,
                    strokeWidth = 3.dp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    HistoryHeaderSection(
                        totalScans = uiState.rawScans.size,
                        onScanNewClick = { navController.navigate(Screen.Scan.route) },
                        onClearHistoryClick = { viewModel.requestClearAll() }
                    )
                }

                item {
                    HistoryTelemetryBand(telemetry = uiState.telemetry)
                }

                item {
                    HistoryFilterAndSearchToolbar(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = { viewModel.onFilterSelected(it) },
                        selectedSort = uiState.selectedSort,
                        onSortSelected = { viewModel.onSortSelected(it) },
                        telemetry = uiState.telemetry
                    )
                }

                if (uiState.rawScans.isEmpty()) {
                    item {
                        HistoryEmptyState(
                            onScanFirstProduce = { navController.navigate(Screen.Scan.route) }
                        )
                    }
                } else if (uiState.filteredScans.isEmpty()) {
                    item {
                        HistoryNoFilterMatchesState(
                            onResetFilters = { viewModel.onResetFilters() }
                        )
                    }
                } else {
                    items(
                        items = uiState.filteredScans,
                        key = { it.id }
                    ) { item ->
                        HistoryCard(
                            item = item,
                            onCardClick = {
                                viewModel.restoreScan(item)
                                navController.navigate(Screen.Analysis.route)
                            },
                            onViewAnalysis = {
                                viewModel.restoreScan(item)
                                navController.navigate(Screen.Analysis.route)
                            },
                            onCompareStorage = {
                                viewModel.restoreScan(item)
                                navController.navigate(Screen.Comparison.route)
                            },
                            onDelete = { viewModel.requestDeleteScan(item.id) }
                        )
                    }
                }

                item {
                    EnvironmentalRemediatorBanner(
                        onOpenWhatIf = {
                            if (uiState.rawScans.isNotEmpty()) {
                                viewModel.restoreScan(uiState.rawScans.first())
                            }
                            navController.navigate(Screen.WhatIf.route)
                        }
                    )
                }

                item {
                    FoodSafetyDisclaimer(
                        backendDisclaimer = "Room DB local persistence. Scientific prediction parameters and shelf-life estimates reflect backend simulation kinetics."
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Clear All Dialog
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Clear All Scan History?",
                            fontWeight = FontWeight.Bold,
                            color = Forest,
                            fontSize = 18.sp
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Permanently remove all ${uiState.rawScans.size} locally cached produce scans from your device.",
                            fontSize = 13.5.sp,
                            color = TextMuted
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF2F2), RoundedCornerShape(FreshIQRadius.radiusSm))
                                .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(FreshIQRadius.radiusSm))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Notice: Scan history is saved strictly within your device's local storage (Room DB). No server-side backup exists. Once cleared, this scan inventory cannot be restored.",
                                fontSize = 12.sp,
                                color = Color(0xFF991B1B),
                                lineHeight = 16.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.confirmClearAll() }
                    ) {
                        Text(
                            text = "Yes, Clear All Scans",
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelClearAll() }) {
                        Text(text = "Cancel", color = TextMuted)
                    }
                }
            )
        }

        // Single Scan Delete Dialog
        if (uiState.deleteCandidateId != null) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelDeleteScan() },
                title = {
                    Text(
                        text = "Delete Scan Record?",
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to permanently delete this scan record from your local Room database?",
                        fontSize = 14.sp,
                        color = TextMuted
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmDeleteScan() }) {
                        Text(
                            text = "Delete",
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDeleteScan() }) {
                        Text(text = "Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}

// =============================================================================
// Header Section
// =============================================================================

@Composable
private fun HistoryHeaderSection(
    totalScans: Int,
    onScanNewClick: () -> Unit,
    onClearHistoryClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(BgSubtle)
                    .border(1.dp, BorderSubtle, CircleShape)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen)
                )
                Text(
                    text = "PANTRY INVENTORY & ARCHIVE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
            }

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Room DB • On-Device",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = Color(0xFF475569)
                )
            }
        }

        Text(
            text = "Scan History & Active Produce",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Forest,
            lineHeight = 32.sp
        )

        Text(
            text = "Track ripening progression, microclimate storage positions, and AI consumption countdowns for all registered produce.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (totalScans > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(FreshIQRadius.radiusFull))
                        .clickable { onClearHistoryClick() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Clear ($totalScans)",
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            FreshPrimaryButton(
                text = "Scan New Item",
                onClick = onScanNewClick,
                icon = Icons.Default.CameraAlt,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// =============================================================================
// Live KPI Telemetry Band
// =============================================================================

@Composable
private fun HistoryTelemetryBand(telemetry: HistoryTelemetry) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiTelemetryCard(
                title = "ACTIVE INVENTORY",
                value = "${telemetry.total} Items",
                subtitle = "Real-time Room DB records",
                icon = Icons.Default.Inbox,
                iconColor = PrimaryGreen,
                accentColor = PrimaryGreen,
                modifier = Modifier.weight(1f)
            )

            KpiTelemetryCard(
                title = "URGENT PEAK WINDOW",
                value = "${telemetry.urgent} Alert${if (telemetry.urgent == 1) "" else "s"}",
                subtitle = "Requires consumption ≤ 48h",
                icon = Icons.Default.Warning,
                iconColor = Color(0xFFDC2626),
                accentColor = Color(0xFFDC2626),
                valueColor = if (telemetry.urgent > 0) Color(0xFFDC2626) else Forest,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiTelemetryCard(
                title = "LIFETIME SCANS",
                value = "${telemetry.total} Scans",
                subtitle = "Avocado v1.2 (MobileNetV3)",
                icon = Icons.Default.BarChart,
                iconColor = Forest,
                accentColor = BorderLight,
                modifier = Modifier.weight(1f)
            )

            KpiTelemetryCard(
                title = "EST. WASTE PREVENTED",
                value = "$${telemetry.wasteSaved}",
                subtitle = "${telemetry.co2Avoided} kg CO₂e avoided",
                icon = Icons.Default.Eco,
                iconColor = PrimaryDark,
                accentColor = LimeGlow,
                valueColor = PrimaryDark,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiTelemetryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    accentColor: Color,
    valueColor: Color = Forest,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
            .background(BgCard)
            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusMd))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = valueColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = TextSubtle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        }
    }
}

// =============================================================================
// Filter & Search Toolbar
// =============================================================================

@Composable
private fun HistoryFilterAndSearchToolbar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedFilter: HistoryFilter,
    onFilterSelected: (HistoryFilter) -> Unit,
    selectedSort: HistorySort,
    onSortSelected: (HistorySort) -> Unit,
    telemetry: HistoryTelemetry
) {
    var sortExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
            .background(BgSubtle)
            .border(1.dp, BorderSubtle, RoundedCornerShape(FreshIQRadius.radiusMd))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, BorderLight, CircleShape)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    textStyle = TextStyle(
                        fontSize = 13.5.sp,
                        color = TextMain
                    ),
                    cursorBrush = SolidColor(PrimaryGreen),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search batch ID, stage, or condition...",
                                fontSize = 13.sp,
                                color = TextSubtle
                            )
                        }
                        innerTextField()
                    }
                )

                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Search",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onSearchQueryChanged("") }
                    )
                }
            }
        }

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val filterOptions = listOf(
                HistoryFilter.ALL to "All (${telemetry.total})",
                HistoryFilter.URGENT to "Needs Attention (${telemetry.urgent})",
                HistoryFilter.CHILLED to "Chilled (${telemetry.chilled})",
                HistoryFilter.FRESH to "Fresh & Firm (${telemetry.fresh})",
                HistoryFilter.OVERRIPE to "Overripe (${telemetry.overripe})"
            )

            filterOptions.forEach { (filter, label) ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) Forest else Color.White)
                        .border(
                            1.dp,
                            if (isSelected) Forest else BorderLight,
                            CircleShape
                        )
                        .clickable { onFilterSelected(filter) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else TextMain,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Sort Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Sort by:",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            Box {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, BorderLight, CircleShape)
                        .clickable { sortExpanded = true }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = selectedSort.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Forest
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Forest,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    HistorySort.values().forEach { sort ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = sort.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedSort == sort) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedSort == sort) PrimaryGreen else TextMain
                                )
                            },
                            onClick = {
                                onSortSelected(sort)
                                sortExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Empty State
// =============================================================================

@Composable
private fun HistoryEmptyState(
    onScanFirstProduce: () -> Unit
) {
    FreshCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BgSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = PrimaryDark,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Produce Scanned Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Forest
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Start tracking your fruit and vegetables to get real-time ripeness diagnostics, decay modeling, and eliminate household food waste.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            FreshPrimaryButton(
                text = "Scan Your First Hass Avocado",
                onClick = onScanFirstProduce,
                icon = Icons.Default.CameraAlt
            )
        }
    }
}

@Composable
private fun HistoryNoFilterMatchesState(
    onResetFilters: () -> Unit
) {
    FreshCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "No produce records matched your search query or selected filter.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
            FreshSecondaryButton(
                text = "Reset Filters",
                onClick = onResetFilters
            )
        }
    }
}

// =============================================================================
// History Card
// =============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryCard(
    item: ScanHistoryItemUi,
    onCardClick: () -> Unit,
    onViewAnalysis: () -> Unit,
    onCompareStorage: () -> Unit,
    onDelete: () -> Unit
) {
    val isBanana = item.prediction.foodType == "banana"
    val isMango = item.prediction.foodType == "mango"
    val isClassificationOnly = isBanana || isMango || !item.prediction.rulAvailable

    val accentColor = when {
        isBanana -> when (item.stage) {
            2 -> Color(0xFFEAB308)
            1 -> Color(0xFF84CC16)
            else -> Color(0xFF16A34A)
        }
        item.isTerminal -> Stage5Fill
        item.isUrgent -> Stage4Fill
        else -> Stage1Fill
    }

    FreshCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Section: Accent Stripe + Thumbnail + Header info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left status accent vertical pill
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(68.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )

                // Thumbnail
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.thumbnailPath.isNotBlank()) {
                        AsyncImage(
                            model = item.thumbnailPath,
                            contentDescription = "Produce thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Item Identity & Badges
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.prediction.itemName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Forest,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "#${item.id.takeLast(6).uppercase(Locale.US)}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            modifier = Modifier
                                .background(BgSubtle, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    RipenessBadge(
                        stage = item.stage,
                        customLabel = "Stage ${item.stage}: ${item.stageLabel}",
                        size = BadgeSize.Compact
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = item.formattedDate,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Context Metadata Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Storage Condition Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = item.storageConditionLabel,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                }

                // Confidence Chip
                Text(
                    text = "${item.confidencePercent}% Confidence",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.5.sp,
                    color = PrimaryDark,
                    modifier = Modifier
                        .background(PrimaryLight, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                // Status Badge Chip
                val statusBg = when {
                    item.isTerminal -> Color(0xFFFEE2E2)
                    item.isUrgent -> Color(0xFFFFEDD5)
                    else -> Color(0xFFDCFCE7)
                }
                val statusText = when {
                    item.isTerminal -> Color(0xFF991B1B)
                    item.isUrgent -> Color(0xFFC2410C)
                    else -> Color(0xFF15803D)
                }
                Text(
                    text = item.statusBadgeText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    color = statusText,
                    modifier = Modifier
                        .background(statusBg, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            // Ripeness Progress Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isBanana) {
                        val bananaColors = listOf(Color(0xFF16A34A), Color(0xFF84CC16), Color(0xFFEAB308))
                        for (s in 0..2) {
                            val segmentColor = if (s <= item.stage) bananaColors[s] else BorderLight
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(5.dp)
                                    .clip(CircleShape)
                                    .background(segmentColor)
                            )
                        }
                    } else {
                        for (s in 1..5) {
                            val segmentColor = if (s <= item.stage) {
                                STAGE_GAUGE_COLORS[s] ?: Stage1Fill
                            } else {
                                BorderLight
                            }
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(5.dp)
                                    .clip(CircleShape)
                                    .background(segmentColor)
                            )
                        }
                    }
                }

                Text(
                    text = when {
                        isBanana -> "Classification Only (3 Stages)"
                        isMango -> "Classification Only (5 Stages)"
                        item.isTerminal -> "Senescent (Overripe)"
                        else -> "~${String.format(Locale.US, "%.1f", item.ambientRulDays)} Days Usable"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = accentColor
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(Color.White)
                        .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                        .clickable { onViewAnalysis() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Forest,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "View Analysis",
                            color = Forest,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }

                if (!isClassificationOnly) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(Color.White)
                            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .clickable { onCompareStorage() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Forest,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Compare Storage",
                                color = Forest,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                        .background(Color.White)
                        .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusSm))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete scan",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// =============================================================================
// Environmental Remediator Banner
// =============================================================================

@Composable
private fun EnvironmentalRemediatorBanner(
    onOpenWhatIf: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
            .background(BgSubtle)
            .border(1.dp, BorderSubtle, RoundedCornerShape(FreshIQRadius.radiusLg))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PrimaryDark,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dynamic Environmental Remediator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Forest
                    )
                    Text(
                        text = "Moving produce from 20°C ambient to 10°C crisper storage decelerates ethylene emission rates by ~68%.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .background(Color.White)
                    .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                    .clickable { onOpenWhatIf() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = Forest,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Open What-If Simulator",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Forest
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Forest,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
