package com.freshiq.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.freshiq.app.ui.screens.analysis.AnalysisScreen
import com.freshiq.app.ui.screens.comparison.ComparisonScreen
import com.freshiq.app.ui.screens.history.HistoryScreen
import com.freshiq.app.ui.screens.home.HomeScreen
import com.freshiq.app.ui.screens.insights.InsightsScreen
import com.freshiq.app.ui.screens.scan.ScanScreen
import com.freshiq.app.ui.screens.whatif.WhatIfScreen

@Composable
fun FreshIQNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Scan.route) {
            ScanScreen(navController = navController)
        }
        composable(Screen.Analysis.route) {
            AnalysisScreen(navController = navController)
        }
        composable(Screen.Comparison.route) {
            ComparisonScreen(navController = navController)
        }
        composable(Screen.WhatIf.route) {
            WhatIfScreen(navController = navController)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }
        composable(Screen.Insights.route) {
            InsightsScreen(navController = navController)
        }
    }
}
