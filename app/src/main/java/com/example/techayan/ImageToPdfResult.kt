package com.techayan.pdfeditor

import android.net.Uri

data class ImageToPdfResult(
    val displayName: String,
    val uri: Uri,
    val imageCount: Int
)
