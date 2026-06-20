package com.techayan.pdftools.navigation

import com.techayan.pdftools.R

sealed class AppDestination(
    val route: String,
    val title: String,
    val iconResId: Int? = null
) {
    data object Splash : AppDestination("splash", "Techayan PDF Tools")
    data object Dashboard : AppDestination("dashboard", "Dashboard", R.drawable.ic_dashboard_24)
    data object ImageToPdf : AppDestination("image_to_pdf", "Image to PDF")
    data object PdfMerge : AppDestination("pdf_merge", "PDF Merge")
    data object PdfSplit : AppDestination("pdf_split", "PDF Split")
    data object PdfCompress : AppDestination("pdf_compress", "PDF Compress")
    data object PdfViewer : AppDestination("pdf_viewer", "PDF Viewer")
    data object PdfToImage : AppDestination("pdf_to_image", "PDF to Image")
    data object ImageCompressor : AppDestination("image_compressor", "Image Compressor")
    data object RecentFiles : AppDestination("recent_files", "Recent Files")
    data object Settings : AppDestination("settings", "Settings", R.drawable.ic_settings_24)
    data object About : AppDestination("about", "About Us", R.drawable.ic_info_24)
    data object PrivacyPolicy : AppDestination("privacy_policy", "Privacy Policy")
    data object TermsConditions : AppDestination("terms_conditions", "Terms & Conditions")

    companion object {
        val bottomNavItems = listOf(Dashboard, Settings, About)
        val all = listOf(
            Splash,
            Dashboard,
            ImageToPdf,
            PdfMerge,
            PdfSplit,
            PdfCompress,
            PdfViewer,
            PdfToImage,
            ImageCompressor,
            RecentFiles,
            Settings,
            About,
            PrivacyPolicy,
            TermsConditions
        )

        fun fromRoute(route: String?): AppDestination {
            return all.firstOrNull { it.route == route } ?: Dashboard
        }
    }
}
