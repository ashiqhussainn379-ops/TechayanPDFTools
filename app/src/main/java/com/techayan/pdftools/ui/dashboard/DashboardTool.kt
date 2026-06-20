package com.techayan.pdftools.ui.dashboard

import androidx.compose.ui.graphics.Color

data class DashboardTool(
    val title: String,
    val description: String,
    val shortName: String,
    val accentColor: Color,
    val category: String,
    val action: DashboardToolAction
)

enum class DashboardToolAction {
    ImageToPdf,
    PdfMerge,
    PdfSplit,
    PdfCompress,
    PdfViewer,
    PdfToImage,
    ImageCompressor,
    RecentFiles,
    Settings,
    About
}
