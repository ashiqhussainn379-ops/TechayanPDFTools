package com.techayan.pdftools.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.techayan.pdftools.navigation.TechayanNavHost
import com.techayan.pdftools.ui.theme.TechayanPdfToolsTheme

@Composable
fun TechayanPdfToolsApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    TechayanPdfToolsTheme(darkTheme = uiState.isDarkMode) {
        TechayanNavHost(
            isDarkMode = uiState.isDarkMode,
            onDarkModeChanged = viewModel::setDarkMode
        )
    }
}
