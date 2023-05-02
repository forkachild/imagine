package com.suhel.imagine.types

import android.graphics.Bitmap
import android.opengl.GLES30

class Dimension(width: Int, height: Int) {

    val values = intArrayOf(width, height)

    val width: Int
        get() = values[0]

    val height: Int
        get() = values[1]

    val aspectRatio: Float
        get() = width.toFloat() / height

    fun fitInside(containerWidth: Int, containerHeight: Int): Dimension {
        if (containerWidth >= width && containerHeight >= height)
            return Dimension(width, height)

        val aspectRatio = width.toFloat() / height
        val containerAspectRatio = containerWidth.toFloat() / containerHeight

        return if (containerAspectRatio > aspectRatio)
            Dimension((width.toFloat() * containerHeight / height).toInt(), containerHeight)
        else
            Dimension(containerWidth, (height.toFloat() * containerWidth / width).toInt())
    }

    fun fitInside(container: Dimension): Dimension {
        return fitInside(container.width, container.height)
    }

    fun bind() {
        GLES30.glViewport(0, 0, width, height)
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