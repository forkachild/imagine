package com.suhel.imagine.types

data class Dimension(val width: Int, val height: Int) {

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

    override fun toString(): String {
        return "${width}x${height}"
    }

}