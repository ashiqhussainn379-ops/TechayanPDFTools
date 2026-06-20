package com.techayan.pdftools.ui.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyScreen() {
    LegalContentScreen(
        title = "Privacy Policy",
        intro = "Techayan PDF Tools is currently a project foundation. No PDF processing, file access, analytics, or account features are implemented in this phase.",
        sections = listOf(
            "Data collection" to "The foundation app does not collect personal information or upload files.",
            "Device permissions" to "No storage, media, camera, or network permissions are requested at this stage.",
            "Future updates" to "This policy should be reviewed before enabling PDF workflows or any service that accesses user files."
        )
    )
}

@Composable
fun TermsConditionsScreen() {
    LegalContentScreen(
        title = "Terms & Conditions",
        intro = "These starter terms describe the foundation build of Techayan PDF Tools. They should be expanded before publishing a production release.",
        sections = listOf(
            "Use of the app" to "The current app provides navigation, dashboard, theme, and informational screens only.",
            "PDF tools" to "Feature cards are placeholders and do not perform PDF conversion, merge, split, compression, viewing, or export yet.",
            "Changes" to "Terms should be updated as product functionality and data handling behavior are implemented."
        )
    )
}

@Composable
private fun LegalContentScreen(
    title: String,
    intro: String,
    sections: List<Pair<String, String>>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = intro,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        sections.forEach { (sectionTitle, body) ->
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = sectionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
