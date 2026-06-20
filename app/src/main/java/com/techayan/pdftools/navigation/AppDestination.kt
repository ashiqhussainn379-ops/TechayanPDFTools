package com.techayan.pdftools.navigation

import com.techayan.pdftools.R

sealed class AppDestination(
    val route: String,
    val title: String,
    val iconResId: Int? = null
) {
    data object Splash : AppDestination("splash", "Techayan PDF Tools")
    data object Dashboard : AppDestination("dashboard", "Dashboard", R.drawable.ic_dashboard_24)
    data object Settings : AppDestination("settings", "Settings", R.drawable.ic_settings_24)
    data object About : AppDestination("about", "About Us", R.drawable.ic_info_24)
    data object PrivacyPolicy : AppDestination("privacy_policy", "Privacy Policy")
    data object TermsConditions : AppDestination("terms_conditions", "Terms & Conditions")

    companion object {
        val bottomNavItems = listOf(Dashboard, Settings, About)
        val all = listOf(Splash, Dashboard, Settings, About, PrivacyPolicy, TermsConditions)

        fun fromRoute(route: String?): AppDestination {
            return all.firstOrNull { it.route == route } ?: Dashboard
        }
    }
}
