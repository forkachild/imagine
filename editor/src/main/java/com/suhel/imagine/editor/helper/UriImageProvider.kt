package com.suhel.imagine.editor.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.suhel.imagine.types.ImageProvider

class UriImageProvider(
    private val context: Context,
    private val uri: Uri,
) : ImageProvider {
    override val bitmap: Bitmap
        get() = BitmapFactory.decodeStream(
            context.contentResolver.openInputStream(uri)
        )
}