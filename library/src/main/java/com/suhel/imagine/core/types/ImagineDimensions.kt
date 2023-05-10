package com.suhel.imagine.core.types

/**
 * Represents 2D dimensions
 *
 * @property width The horizontal dimension in pixels
 * @property height The vertical dimension in pixels
 */
internal data class ImagineDimensions(
    val width: Int,
    val height: Int
) {

    /**
     * The ratio between the width and the height
     */
    val aspectRatio: Float
        get() = width.toFloat() / height

    /**
     * Scales the dimensions to fit into the given dimensions while
     * maintaining the aspect ratio
     *
     * @param containerWidth The width in pixels of the container
     * @param containerHeight The height in pixels of the container
     *
     * @return A scaled [ImagineDimensions]
     */
    fun fitInside(containerWidth: Int, containerHeight: Int): ImagineDimensions {
        if (containerWidth >= width && containerHeight >= height)
            return ImagineDimensions(width, height)

        val aspectRatio = width.toFloat() / height
        val containerAspectRatio = containerWidth.toFloat() / containerHeight

        return if (containerAspectRatio > aspectRatio)
            ImagineDimensions(
                (width.toFloat() * containerHeight / height).toInt(),
                containerHeight
            )
        else
            ImagineDimensions(
                containerWidth,
                (height.toFloat() * containerWidth / width).toInt()
            )
    }

    /**
     * Scales the dimensions to fit into the given dimensions while
     * maintaining the aspect ratio
     *
     * @param container The [ImagineDimensions] of the container
     *
     * @return A scaled [ImagineDimensions]
     */
    fun fitInside(container: ImagineDimensions): ImagineDimensions {
        return fitInside(container.width, container.height)
    }

    override fun toString(): String {
        return "${width}x${height}"
    }

}