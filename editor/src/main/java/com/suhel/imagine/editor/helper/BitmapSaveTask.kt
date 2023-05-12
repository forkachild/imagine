package com.suhel.imagine.editor.helper

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.suhel.imagine.editor.model.BitmapSaveException
import com.suhel.imagine.editor.model.BitmapSaveFormat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BitmapSaveTask(
    private val context: Context,
    private val bitmap: Bitmap,
    private val format: BitmapSaveFormat,
    private val onStart: () -> Unit,
    private val onSuccess: () -> Unit,
    private val onError: (BitmapSaveException) -> Unit,
) : Runnable {

    private val mainThread = Handler(Looper.getMainLooper())

    override fun run() {
        val timestamp = SimpleDateFormat(
            "dd-MM-yyyy-HH-mm-SSSS",
            Locale.getDefault()
        ).format(Date())
        val fileName = "imagine-$timestamp"

        val values = ContentValues().apply {
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Images.ImageColumns.MIME_TYPE, format.mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                put(MediaStore.Images.ImageColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        if (uri == null) {
            postError(BitmapSaveException.MediaStoreRecord())
            return
        }

        postStart()

        val outputStream = contentResolver.openOutputStream(uri)

        if (outputStream == null) {
            postError(BitmapSaveException.OutputStream())
            return
        }

        val saveResult = bitmap.compress(format.compressFormat, 95, outputStream)

        outputStream.close()

        if (!saveResult) {
            postError(BitmapSaveException.Save())
            return
        }

        postSuccess()
    }

    private fun postStart() {
        mainThread.post(onStart)
    }

    private fun postSuccess() {
        mainThread.post(onSuccess)
    }

    private fun postError(exception: BitmapSaveException) {
        mainThread.post {
            onError(exception)
        }
    }

}