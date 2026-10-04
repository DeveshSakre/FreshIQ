package com.freshiq.app.ui.navigation

sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Home")
    object Scan : Screen("scan", "Scan Produce")
    object Analysis : Screen("analysis", "Analysis")
    object Comparison : Screen("comparison", "Comparison")
    object WhatIf : Screen("what_if", "What-If Lab")
    object History : Screen("history", "History")
    object Insights : Screen("insights", "ML Insights")

    companion object {
        val bottomNavItems = listOf(Home, Scan, History, WhatIf, Insights)
    }
}
