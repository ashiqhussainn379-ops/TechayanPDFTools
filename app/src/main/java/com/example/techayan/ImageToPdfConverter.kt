package com.techayan.pdfeditor

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

object ImageToPdfConverter {

    private const val OUTPUT_FOLDER = "TechayanPDF"
    private const val MIME_PDF = "application/pdf"
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val PAGE_MARGIN = 32
    private const val MAX_IMAGE_SIDE = 2400

    fun createPdf(context: Context, imageUris: List<Uri>): ImageToPdfResult {
        require(imageUris.isNotEmpty()) { "Please select at least one image" }

        val displayName = createFileName()
        val outputUri = createOutputUri(context, displayName)
        val document = PdfDocument()

        try {
            imageUris.forEachIndexed { index, uri ->
                val bitmap = loadImage(context.contentResolver, uri)
                try {
                    val pageInfo = PdfDocument.PageInfo.Builder(
                        PAGE_WIDTH,
                        PAGE_HEIGHT,
                        index + 1
                    ).create()
                    val page = document.startPage(pageInfo)
                    drawImageOnPage(page.canvas, bitmap)
                    document.finishPage(page)
                } finally {
                    bitmap.recycle()
                }
            }

            context.contentResolver.openOutputStream(outputUri, "w")?.use { output ->
                document.writeTo(output)
            } ?: error("Unable to save PDF")

            markFileComplete(context, outputUri)

            return ImageToPdfResult(
                displayName = displayName,
                uri = outputUri,
                imageCount = imageUris.size
            )
        } catch (throwable: Throwable) {
            deleteFailedOutput(context, outputUri)
            throw throwable
        } finally {
            document.close()
        }
    }

    private fun createFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "Techayan_Image_$timestamp.pdf"
    }

    private fun createOutputUri(context: Context, displayName: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, MIME_PDF)
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOCUMENTS}/$OUTPUT_FOLDER"
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            context.contentResolver.insert(
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                values
            ) ?: error("Unable to create output file")
        } else {
            val documents = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
            )
            val folder = File(documents, OUTPUT_FOLDER)
            if (!folder.exists() && !folder.mkdirs()) {
                error("Unable to create Documents/$OUTPUT_FOLDER folder")
            }

            val file = File(folder, displayName)
            FileOutputStream(file).close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }
    }

    private fun markFileComplete(context: Context, uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            context.contentResolver.update(uri, values, null, null)
        }
    }

    private fun deleteFailedOutput(context: Context, uri: Uri) {
        runCatching {
            context.contentResolver.delete(uri, null, null)
        }
    }

    private fun loadImage(resolver: ContentResolver, uri: Uri): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            decodeWithImageDecoder(resolver, uri)?.let { return it }
        }

        val bounds = readImageBounds(resolver, uri)
        val decoded = decodeWithBitmapFactory(resolver, uri, bounds.outWidth, bounds.outHeight)
            ?: error("Unable to decode selected image. Please select the image again.")

        return rotateBitmapIfRequired(resolver, uri, decoded)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun decodeWithImageDecoder(resolver: ContentResolver, uri: Uri): Bitmap? {
        return runCatching {
            val source = ImageDecoder.createSource(resolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.setTargetSampleSize(
                    calculateSampleSize(info.size.width, info.size.height)
                )
            }
        }.getOrNull()
    }

    private fun readImageBounds(resolver: ContentResolver, uri: Uri): BitmapFactory.Options {
        val fileDescriptorBounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        val readFileDescriptor = decodeFileDescriptor(
            resolver = resolver,
            uri = uri,
            options = fileDescriptorBounds
        )
        if (readFileDescriptor && fileDescriptorBounds.hasValidSize()) {
            return fileDescriptorBounds
        }

        val streamBounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        val readStream = decodeStream(
            resolver = resolver,
            uri = uri,
            options = streamBounds
        )
        if (readStream && streamBounds.hasValidSize()) {
            return streamBounds
        }

        if (readFileDescriptor || readStream) {
            error("Selected file is not a valid image")
        }

        error("Unable to read selected image. Please select the image again.")
    }

    private fun BitmapFactory.Options.hasValidSize(): Boolean {
        return outWidth > 0 && outHeight > 0
    }

    private fun decodeWithBitmapFactory(
        resolver: ContentResolver,
        uri: Uri,
        width: Int,
        height: Int
    ): Bitmap? {
        val sampleSize = calculateSampleSize(width, height)

        return decodeBitmapFromFileDescriptor(resolver, uri, sampleSize)
            ?: decodeBitmapFromStream(resolver, uri, sampleSize)
    }

    private fun decodeBitmapFromFileDescriptor(
        resolver: ContentResolver,
        uri: Uri,
        sampleSize: Int
    ): Bitmap? {
        val options = createDecodeOptions(sampleSize)
        return runCatching {
            resolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, options)
            }
        }.getOrNull()
    }

    private fun decodeBitmapFromStream(
        resolver: ContentResolver,
        uri: Uri,
        sampleSize: Int
    ): Bitmap? {
        val options = createDecodeOptions(sampleSize)
        return runCatching {
            resolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
        }.getOrNull()
    }

    private fun createDecodeOptions(sampleSize: Int): BitmapFactory.Options {
        return BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
    }

    private fun decodeFileDescriptor(
        resolver: ContentResolver,
        uri: Uri,
        options: BitmapFactory.Options
    ): Boolean {
        return runCatching {
            resolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, options)
                true
            } ?: false
        }.getOrDefault(false)
    }

    private fun decodeStream(
        resolver: ContentResolver,
        uri: Uri,
        options: BitmapFactory.Options
    ): Boolean {
        return runCatching {
            resolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
                true
            } ?: false
        }.getOrDefault(false)
    }

    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        var currentWidth = width
        var currentHeight = height

        while (currentWidth / 2 >= MAX_IMAGE_SIDE || currentHeight / 2 >= MAX_IMAGE_SIDE) {
            currentWidth /= 2
            currentHeight /= 2
            sampleSize *= 2
        }

        return sampleSize.coerceAtLeast(1)
    }

    private fun rotateBitmapIfRequired(
        resolver: ContentResolver,
        uri: Uri,
        bitmap: Bitmap
    ): Bitmap {
        val orientation = runCatching {
            resolver.openInputStream(uri)?.use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        if (degrees == 0f) return bitmap

        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return rotated
    }

    private fun drawImageOnPage(canvas: Canvas, bitmap: Bitmap) {
        canvas.drawColor(Color.WHITE)

        val availableWidth = PAGE_WIDTH - PAGE_MARGIN * 2
        val availableHeight = PAGE_HEIGHT - PAGE_MARGIN * 2
        val scale = min(
            availableWidth.toFloat() / bitmap.width.toFloat(),
            availableHeight.toFloat() / bitmap.height.toFloat()
        )

        val targetWidth = (bitmap.width * scale).roundToInt()
        val targetHeight = (bitmap.height * scale).roundToInt()
        val left = (PAGE_WIDTH - targetWidth) / 2f
        val top = (PAGE_HEIGHT - targetHeight) / 2f

        val target = RectF(left, top, left + targetWidth, top + targetHeight)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bitmap, null, target, paint)
    }
}
