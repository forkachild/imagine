package com.suhel.imagine.core.objects

import com.suhel.imagine.core.types.ImagineDimensions

/**
 * Represents a viewport in OpenGL. Since there is no underlying resource
 * this just holds the dimensions
 *
 * @property dimensions The [ImagineDimensions] of the viewport
 */
internal class ImagineViewport(
    val dimensions: ImagineDimensions,
)
