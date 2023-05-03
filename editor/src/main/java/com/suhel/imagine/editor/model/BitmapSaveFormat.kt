package com.suhel.imagine.editor.model

import android.graphics.Bitmap

enum class BitmapSaveFormat {
    PNG,
    JPEG;

    val mimeType: String
        get() = when (this) {
            PNG -> "image/png"
            JPEG -> "image/jpg"
        }

    val compressFormat: Bitmap.CompressFormat
        get() = when (this) {
            PNG -> Bitmap.CompressFormat.PNG
            JPEG -> Bitmap.CompressFormat.JPEG
        }

}