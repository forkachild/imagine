package com.suhel.imagine.core.types

import com.suhel.imagine.core.objects.ImagineShader

/**
 * Abstract for a image processing layer
 */
interface ImagineLayer {

    /**
     * Source code of the fragment shader code snippet
     */
    val source: String

    /**
     * Intensity of application of this layer
     */
    val intensity: Float

    /**
     * Type of blending to apply between this Layer and the previous Layer
     */
    val blendMode: ImagineBlendMode

    /**
     * Called during shader creation to bind any uniform
     */
    fun create(program: ImagineShader.Program) {}

    /**
     * Called during rendering to bind any uniform
     */
    fun bind(program: ImagineShader.Program) {}
}