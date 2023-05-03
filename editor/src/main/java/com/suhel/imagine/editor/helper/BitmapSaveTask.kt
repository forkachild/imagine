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
import java.text.SimpleDateFormat
import java.util.*

class BitmapSaveTask(
    private val context: Context,
    private val bitmap: Bitmap,
    private val format: BitmapSaveFormat,
    private val onSuccess: () -> Unit,
    private val onError: (BitmapSaveException) -> Unit,
) : Runnable {

    private val mainThread = Handler(Looper.getMainLooper())

    override fun run() {
        val fileName =
            SimpleDateFormat("dd-MM-yyyy-HH-mm-ss-Z", Locale.getDefault()).format(Date())

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        }

        val contentResolver = context.contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        if (uri == null) {
            postError(BitmapSaveException.MediaStoreRecord())
            return
        }

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

    private fun postSuccess() {
        mainThread.post(onSuccess)
    }

    private fun postError(exception: BitmapSaveException) {
        mainThread.post {
            onError(exception)
        }
    }

}