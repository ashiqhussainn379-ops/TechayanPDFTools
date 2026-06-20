package com.techayan.pdftools.ui.imagetopdf

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class ImageToPdfRepository(
    private val context: Context
) {

    suspend fun generatePdf(images: List<SelectedImage>): GeneratedPdf = withContext(Dispatchers.IO) {
        require(images.isNotEmpty()) { "Select at least one image before generating a PDF." }

        val fileName = "TechayanPDF_${timestamp()}.pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writePdfToMediaStore(fileName, images)
        } else {
            writePdfToPublicDocuments(fileName, images)
        }
    }

    private fun writePdfToMediaStore(
        fileName: String,
        images: List<SelectedImage>
    ): GeneratedPdf {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, PDF_MIME_TYPE)
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_DOCUMENTS}/$OUTPUT_DIRECTORY"
            )
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val pdfUri = resolver.insert(collection, values)
            ?: throw IOException("Unable to create a PDF file in Documents/$OUTPUT_DIRECTORY.")

        try {
            resolver.openOutputStream(pdfUri)?.use { outputStream ->
                writePdf(images, outputStream)
            } ?: throw IOException("Unable to open the PDF output stream.")

            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(pdfUri, values, null, null)

            return GeneratedPdf(
                uri = pdfUri,
                fileName = fileName,
                savedLocation = "Documents/$OUTPUT_DIRECTORY/$fileName"
            )
        } catch (exception: Exception) {
            resolver.delete(pdfUri, null, null)
            throw exception
        }
    }

    private fun writePdfToPublicDocuments(
        fileName: String,
        images: List<SelectedImage>
    ): GeneratedPdf {
        val documentsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val outputDirectory = File(documentsDirectory, OUTPUT_DIRECTORY)

        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw IOException("Unable to create Documents/$OUTPUT_DIRECTORY.")
        }

        val outputFile = File(outputDirectory, fileName)
        FileOutputStream(outputFile).use { outputStream ->
            writePdf(images, outputStream)
        }

        val pdfUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile
        )

        return GeneratedPdf(
            uri = pdfUri,
            fileName = fileName,
            savedLocation = "Documents/$OUTPUT_DIRECTORY/$fileName"
        )
    }

    private fun writePdf(
        images: List<SelectedImage>,
        outputStream: java.io.OutputStream
    ) {
        PdfDocument().use { pdfDocument ->
            images.forEachIndexed { index, image ->
                val bitmap = decodeBitmapForPdf(image.uri)
                    ?: throw IOException("Unable to decode ${image.name}.")

                try {
                    val isLandscape = bitmap.width > bitmap.height
                    val pageWidth = if (isLandscape) A4_LONG_EDGE else A4_SHORT_EDGE
                    val pageHeight = if (isLandscape) A4_SHORT_EDGE else A4_LONG_EDGE
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                    val page = pdfDocument.startPage(pageInfo)

                    drawImagePage(
                        canvas = page.canvas,
                        bitmap = bitmap,
                        pageWidth = pageWidth,
                        pageHeight = pageHeight
                    )

                    pdfDocument.finishPage(page)
                } finally {
                    bitmap.recycle()
                }
            }

            pdfDocument.writeTo(outputStream)
        }
    }

    private fun drawImagePage(
        canvas: Canvas,
        bitmap: Bitmap,
        pageWidth: Int,
        pageHeight: Int
    ) {
        canvas.drawColor(Color.WHITE)

        val contentWidth = pageWidth - (PAGE_MARGIN * 2)
        val contentHeight = pageHeight - (PAGE_MARGIN * 2)
        val scale = min(
            contentWidth / bitmap.width.toFloat(),
            contentHeight / bitmap.height.toFloat()
        )
        val targetWidth = bitmap.width * scale
        val targetHeight = bitmap.height * scale
        val left = (pageWidth - targetWidth) / 2f
        val top = (pageHeight - targetHeight) / 2f
        val destination = RectF(left, top, left + targetWidth, top + targetHeight)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        canvas.drawBitmap(bitmap, null, destination, paint)
    }

    private fun decodeBitmapForPdf(uri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val sampleSize = calculateSampleSize(
                    width = info.size.width,
                    height = info.size.height,
                    maxSize = MAX_BITMAP_DIMENSION
                )
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.setTargetSampleSize(sampleSize)
            }
        } else {
            decodeBitmapWithFactory(context.contentResolver, uri, MAX_BITMAP_DIMENSION)
        }
    }

    private fun decodeBitmapWithFactory(
        resolver: ContentResolver,
        uri: Uri,
        maxSize: Int
    ): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        resolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }

        val sampleSize = calculateSampleSize(
            width = options.outWidth,
            height = options.outHeight,
            maxSize = maxSize
        )

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }

        return resolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, decodeOptions)
        }
    }

    private fun calculateSampleSize(
        width: Int,
        height: Int,
        maxSize: Int
    ): Int {
        var sampleSize = 1
        var largestEdge = max(width, height)
        while (largestEdge / sampleSize > maxSize) {
            sampleSize *= 2
        }
        return sampleSize.coerceAtLeast(1)
    }

    private fun timestamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    }

    companion object {
        const val PDF_MIME_TYPE = "application/pdf"
        const val OUTPUT_DIRECTORY = "TechayanPDF"
        private const val A4_SHORT_EDGE = 595
        private const val A4_LONG_EDGE = 842
        private const val PAGE_MARGIN = 36
        private const val MAX_BITMAP_DIMENSION = 2480
    }
}
