package com.techayan.pdftools.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val appVersion: String = "1.0",
    val legalItems: List<SettingsItem> = listOf(
        SettingsItem.PrivacyPolicy,
        SettingsItem.TermsConditions
    )
)

sealed class SettingsItem(
    val title: String,
    val subtitle: String
) {
    data object PrivacyPolicy : SettingsItem(
        title = "Privacy Policy",
        subtitle = "Review how the app will handle data."
    )

    data object TermsConditions : SettingsItem(
        title = "Terms & Conditions",
        subtitle = "Read the terms for using Techayan PDF Tools."
    )
}

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
}
