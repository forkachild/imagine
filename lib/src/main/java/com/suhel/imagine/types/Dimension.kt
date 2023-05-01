package com.suhel.imagine.types

import android.graphics.Bitmap
import com.suhel.imagine.Constants

data class Dimension(
    val width: Int,
    val height: Int,
) {

    val aspectRatio: Float
        get() = width.toFloat() / height

    fun fitIn(containerWidth: Int, containerHeight: Int): Dimension {
        if (containerWidth >= width && containerHeight >= height)
            return Dimension(width, height)

        val aspectRatio = width.toFloat() / height
        val containerAspectRatio = containerWidth.toFloat() / containerHeight

        return if (containerAspectRatio > aspectRatio)
            Dimension((width.toFloat() * containerHeight / height).toInt(), containerHeight)
        else
            Dimension(containerWidth, (height.toFloat() * containerWidth / width).toInt())
    }

    fun fitIn(container: Dimension): Dimension {
        return fitIn(container.width, container.height)
    }

    override fun toString(): String {
        return "${width}x${height}"
    }

    companion object {

        fun fromBitmap(bitmap: Bitmap): Dimension {
            return Dimension(bitmap.width, bitmap.height)
        }

    }

}