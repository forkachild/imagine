package com.suhel.imagine.editor.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.suhel.imagine.core.types.ImagineImageProvider

class UriImageProvider(
    private val context: Context,
    private val uri: Uri,
) : ImagineImageProvider {
    override val bitmap: Bitmap
        get() = BitmapFactory.decodeStream(
            context.contentResolver.openInputStream(uri)
        )
}