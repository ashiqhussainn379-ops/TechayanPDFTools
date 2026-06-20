package com.techayan.pdftools.ui.features

import androidx.compose.ui.graphics.Color
import com.techayan.pdftools.ui.dashboard.DashboardToolAction

data class FeatureDestination(
    val title: String,
    val subtitle: String,
    val badge: String,
    val category: String,
    val accentColor: Color,
    val highlights: List<String>
) {
    companion object {
        fun fromAction(action: DashboardToolAction): FeatureDestination {
            return when (action) {
                DashboardToolAction.ImageToPdf -> FeatureDestination(
                    title = "Image to PDF",
                    subtitle = "Convert JPG, PNG, and WEBP images into organized PDF documents.",
                    badge = "IMG",
                    category = "Convert",
                    accentColor = Color(0xFF2563EB),
                    highlights = listOf("Multi-image selection", "Page ordering", "Export controls")
                )

                DashboardToolAction.PdfMerge -> FeatureDestination(
                    title = "PDF Merge",
                    subtitle = "Combine multiple PDF documents into one clean file.",
                    badge = "MRG",
                    category = "Organize",
                    accentColor = Color(0xFF7C3AED),
                    highlights = listOf("File queue", "Drag order", "Single output")
                )

                DashboardToolAction.PdfSplit -> FeatureDestination(
                    title = "PDF Split",
                    subtitle = "Extract selected pages or split large documents into smaller files.",
                    badge = "SPL",
                    category = "Organize",
                    accentColor = Color(0xFF059669),
                    highlights = listOf("Page ranges", "Preview support", "Batch output")
                )

                DashboardToolAction.PdfCompress -> FeatureDestination(
                    title = "PDF Compress",
                    subtitle = "Reduce PDF size while keeping documents readable and shareable.",
                    badge = "ZIP",
                    category = "Optimize",
                    accentColor = Color(0xFFEA580C),
                    highlights = listOf("Quality presets", "Size estimates", "Fast sharing")
                )

                DashboardToolAction.PdfViewer -> FeatureDestination(
                    title = "PDF Viewer",
                    subtitle = "Open, browse, and review PDF documents in a focused viewer.",
                    badge = "PDF",
                    category = "Read",
                    accentColor = Color(0xFFDC2626),
                    highlights = listOf("Recent documents", "Page navigation", "Clean reading")
                )

                DashboardToolAction.PdfToImage -> FeatureDestination(
                    title = "PDF to Image",
                    subtitle = "Export PDF pages into image files for sharing or archiving.",
                    badge = "PNG",
                    category = "Convert",
                    accentColor = Color(0xFF0891B2),
                    highlights = listOf("Page selection", "Image formats", "Output folder")
                )

                DashboardToolAction.ImageCompressor -> FeatureDestination(
                    title = "Image Compressor",
                    subtitle = "Optimize images before sharing or adding them to documents.",
                    badge = "CMP",
                    category = "Optimize",
                    accentColor = Color(0xFFDB2777),
                    highlights = listOf("Quality slider", "Preview size", "Batch compress")
                )

                DashboardToolAction.RecentFiles,
                DashboardToolAction.Settings,
                DashboardToolAction.About -> FeatureDestination(
                    title = "Techayan PDF Tools",
                    subtitle = "Professional PDF utilities for Android.",
                    badge = "PDF",
                    category = "App",
                    accentColor = Color(0xFF2563EB),
                    highlights = emptyList()
                )
            }
        }
    }
}
