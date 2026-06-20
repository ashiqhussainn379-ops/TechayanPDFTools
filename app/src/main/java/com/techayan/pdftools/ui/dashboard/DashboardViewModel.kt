package com.techayan.pdftools.ui.dashboard

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DashboardUiState(
    val tools: List<DashboardTool> = emptyList()
)

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            tools = listOf(
                DashboardTool(
                    title = "Image to PDF",
                    description = "Convert photos into polished PDF documents.",
                    shortName = "IMG",
                    accentColor = Color(0xFF2563EB),
                    category = "Convert",
                    action = DashboardToolAction.ImageToPdf
                ),
                DashboardTool(
                    title = "PDF Merge",
                    description = "Combine multiple files into a single PDF.",
                    shortName = "MRG",
                    accentColor = Color(0xFF7C3AED),
                    category = "Organize",
                    action = DashboardToolAction.PdfMerge
                ),
                DashboardTool(
                    title = "PDF Split",
                    description = "Separate pages into smaller PDF files.",
                    shortName = "SPL",
                    accentColor = Color(0xFF059669),
                    category = "Organize",
                    action = DashboardToolAction.PdfSplit
                ),
                DashboardTool(
                    title = "PDF Compress",
                    description = "Reduce file size while preserving quality.",
                    shortName = "ZIP",
                    accentColor = Color(0xFFEA580C),
                    category = "Optimize",
                    action = DashboardToolAction.PdfCompress
                ),
                DashboardTool(
                    title = "PDF Viewer",
                    description = "Open and review PDF documents.",
                    shortName = "PDF",
                    accentColor = Color(0xFFDC2626),
                    category = "Read",
                    action = DashboardToolAction.PdfViewer
                ),
                DashboardTool(
                    title = "PDF to Image",
                    description = "Export PDF pages as image files.",
                    shortName = "PNG",
                    accentColor = Color(0xFF0891B2),
                    category = "Convert",
                    action = DashboardToolAction.PdfToImage
                ),
                DashboardTool(
                    title = "Image Compressor",
                    description = "Prepare image optimization before PDF export.",
                    shortName = "CMP",
                    accentColor = Color(0xFFDB2777),
                    category = "Optimize",
                    action = DashboardToolAction.ImageCompressor
                ),
                DashboardTool(
                    title = "Recent Files",
                    description = "Review documents and images you worked with recently.",
                    shortName = "REC",
                    accentColor = Color(0xFF4F46E5),
                    category = "Library",
                    action = DashboardToolAction.RecentFiles
                ),
                DashboardTool(
                    title = "Settings",
                    description = "Manage appearance, app preferences, and legal links.",
                    shortName = "SET",
                    accentColor = Color(0xFF64748B),
                    category = "App",
                    action = DashboardToolAction.Settings
                ),
                DashboardTool(
                    title = "About",
                    description = "Learn about Techayan PDF Tools and its foundation.",
                    shortName = "i",
                    accentColor = Color(0xFF0D9488),
                    category = "App",
                    action = DashboardToolAction.About
                )
            )
        )
    )

    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
}
