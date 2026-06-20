package com.techayan.pdfeditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.techayan.ui.theme.TechayanTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TechayanTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ImageToPdfScreen()
                }
            }
        }
    }
}
