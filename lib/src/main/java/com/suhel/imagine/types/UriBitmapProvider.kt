package com.suhel.imagine.types

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

class UriBitmapProvider(
    private val context: Context,
    private val uri: Uri,
) : BitmapProvider {
    override val bitmap: Bitmap
        get() = BitmapFactory.decodeStream(
            context.contentResolver.openInputStream(uri)
        )
}