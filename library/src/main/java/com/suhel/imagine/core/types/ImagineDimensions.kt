package com.suhel.imagine.core.types

internal data class ImagineDimensions(val width: Int, val height: Int) {

    val aspectRatio: Float
        get() = width.toFloat() / height

    fun fitInside(containerWidth: Int, containerHeight: Int): ImagineDimensions {
        if (containerWidth >= width && containerHeight >= height)
            return ImagineDimensions(width, height)

        val aspectRatio = width.toFloat() / height
        val containerAspectRatio = containerWidth.toFloat() / containerHeight

        return if (containerAspectRatio > aspectRatio)
            ImagineDimensions((width.toFloat() * containerHeight / height).toInt(), containerHeight)
        else
            ImagineDimensions(containerWidth, (height.toFloat() * containerWidth / width).toInt())
    }

    fun fitInside(container: ImagineDimensions): ImagineDimensions {
        return fitInside(container.width, container.height)
    }

    override fun toString(): String {
        return "${width}x${height}"
    }

}