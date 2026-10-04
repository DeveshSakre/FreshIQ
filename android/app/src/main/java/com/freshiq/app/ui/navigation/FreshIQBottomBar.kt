package com.freshiq.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.freshiq.app.ui.theme.BgCard
import com.freshiq.app.ui.theme.BorderLight
import com.freshiq.app.ui.theme.Forest
import com.freshiq.app.ui.theme.FreshIQTheme
import com.freshiq.app.ui.theme.LimeGlow
import com.freshiq.app.ui.theme.PrimaryGreen
import com.freshiq.app.ui.theme.PrimaryLight
import com.freshiq.app.ui.theme.TextMuted

private data class NavigationItem(
    val screen: Screen,
    val icon: ImageVector,
    val isProminent: Boolean = false
)

@Composable
fun FreshIQBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    FreshIQBottomBarContent(
        currentRoute = currentRoute,
        onNavigate = { route ->
            if (currentRoute != route) {
                navController.navigate(route) {
                    popUpTo(Screen.Home.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    )
}

@Composable
fun FreshIQBottomBarContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember {
        listOf(
            NavigationItem(Screen.Home, Icons.Default.Home),
            NavigationItem(Screen.Scan, Icons.Default.CameraAlt, isProminent = true),
            NavigationItem(Screen.History, Icons.Default.History),
            NavigationItem(Screen.WhatIf, Icons.Default.Tune),
            NavigationItem(Screen.Insights, Icons.Default.Book)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BgCard)
            .border(width = 1.dp, color = BorderLight)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.screen.route

                if (item.isProminent) {
                    // Prominent Scan Produce CTA button in the center
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .offset(y = (-8).dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onNavigate(item.screen.route)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Forest)
                                .border(2.dp, if (isSelected) LimeGlow else PrimaryGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.screen.label,
                                tint = if (isSelected) LimeGlow else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = "Scan",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) Forest else TextMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                } else {
                    // Standard navigation item
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .clickable { onNavigate(item.screen.route) }
                            .padding(vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) PrimaryLight else Color.Transparent)
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.screen.label,
                                tint = if (isSelected) Forest else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = item.screen.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Forest else TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FreshIQBottomBarPreview() {
    FreshIQTheme {
        FreshIQBottomBarContent(
            currentRoute = Screen.Scan.route,
            onNavigate = {}
        )
    }
}
