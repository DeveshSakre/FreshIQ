package com.freshiq.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.freshiq.app.ui.components.FreshIQTopBar
import com.freshiq.app.ui.navigation.FreshIQBottomBar
import com.freshiq.app.ui.navigation.FreshIQNavHost
import com.freshiq.app.ui.navigation.Screen
import com.freshiq.app.ui.theme.FreshIQTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreshIQTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val isTopLevelDestination = Screen.bottomNavItems.any { it.route == currentRoute }
                val canNavigateBack = !isTopLevelDestination && currentRoute != null && currentRoute != Screen.Home.route

                val screenTitle = when (currentRoute) {
                    Screen.Analysis.route -> "Produce Analysis"
                    Screen.Comparison.route -> "Shelf-Life Comparison"
                    Screen.WhatIf.route -> "What-If Storage Lab"
                    Screen.History.route -> "Scan History"
                    Screen.Insights.route -> "ML Insights"
                    else -> null
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        FreshIQTopBar(
                            title = screenTitle,
                            canNavigateBack = canNavigateBack,
                            onNavigateBack = { navController.navigateUp() }
                        )
                    },
                    bottomBar = {
                        FreshIQBottomBar(navController = navController)
                    }
                ) { innerPadding ->
                    FreshIQNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
