package com.suhel.imagine.editor.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.suhel.imagine.core.types.ImagineImageProvider

class DrawableImageProvider(
    private val context: Context,
    @DrawableRes private val resId: Int
) : ImagineImageProvider {

    override val bitmap: Bitmap
        get() = (ResourcesCompat.getDrawable(
            context.resources,
            resId,
            null
        ) as BitmapDrawable).bitmap

}