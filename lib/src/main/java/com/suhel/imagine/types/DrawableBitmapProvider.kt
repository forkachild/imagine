package com.suhel.imagine.types

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat

class DrawableBitmapProvider(
    private val context: Context,
    @DrawableRes private val resId: Int
) : BitmapProvider {

    override val bitmap: Bitmap
        get() = (ResourcesCompat.getDrawable(
            context.resources,
            resId,
            null
        ) as BitmapDrawable).bitmap

}