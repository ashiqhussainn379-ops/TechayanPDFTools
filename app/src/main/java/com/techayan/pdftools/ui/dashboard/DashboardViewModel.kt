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
                    accentColor = Color(0xFF2563EB)
                ),
                DashboardTool(
                    title = "PDF Merge",
                    description = "Combine multiple files into a single PDF.",
                    shortName = "MRG",
                    accentColor = Color(0xFF7C3AED)
                ),
                DashboardTool(
                    title = "PDF Split",
                    description = "Separate pages into smaller PDF files.",
                    shortName = "SPL",
                    accentColor = Color(0xFF059669)
                ),
                DashboardTool(
                    title = "PDF Compress",
                    description = "Reduce file size while preserving quality.",
                    shortName = "ZIP",
                    accentColor = Color(0xFFEA580C)
                ),
                DashboardTool(
                    title = "PDF Viewer",
                    description = "Open and review PDF documents.",
                    shortName = "PDF",
                    accentColor = Color(0xFFDC2626)
                ),
                DashboardTool(
                    title = "PDF to Image",
                    description = "Export PDF pages as image files.",
                    shortName = "PNG",
                    accentColor = Color(0xFF0891B2)
                )
            )
        )
    )

    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
}
