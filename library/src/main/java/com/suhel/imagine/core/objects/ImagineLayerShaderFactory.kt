package com.suhel.imagine.core.objects

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.suhel.imagine.R
import com.suhel.imagine.core.objects.ImagineLayerShaderFactory.Companion.create
import com.suhel.imagine.core.types.ImagineLayer
import com.suhel.imagine.util.readRawRes
import kotlin.reflect.KClass

/**
 * A helper utility that supplies [ImagineLayerShader] during rendering
 *
 * This class should only be constructed from the companion method [create]
 *
 * @property bypassShader The shader that copies pixels from a texture
 * @property vsQuad The partial pre-compiled Vertex Shader to link with
 * new layer Fragment Shaders at runtime
 */
internal class ImagineLayerShaderFactory @VisibleForTesting constructor(
    val bypassShader: ImagineLayerShader,
    private val vsQuad: ImagineShader.Compiled,
    private val fsLayerBaseSrc: String,
) {

    /**
     * Indicates whether this underlying resource is released
     */
    private var isReleased: Boolean = false

    /**
     * Maps [ImagineLayer] descendant class names to their corresponding
     * [ImagineLayerShader] for ease of access. Acts as a cache
     */
    private val layerShaderMap: MutableMap<KClass<out ImagineLayer>, ImagineLayerShader> =
        mutableMapOf()

    /**
     * Tries to supply an [ImagineLayerShader] for a particular [ImagineLayer].
     * It either tries to fetch from the cache, or else tries creating it
     *
     * @param layer The [ImagineLayer] for which the shader is required
     *
     * @return An [ImagineLayerShader] if possible, null otherwise
     */
    fun getLayerShader(layer: ImagineLayer): ImagineLayerShader? = releaseSafe {
        layerShaderMap[layer::class] ?: run {
            // Optimization to prevent resize
            val fsLayerSrc = StringBuilder(
                fsLayerBaseSrc.length + layer.source.length + 2
            ).apply {
                append(fsLayerBaseSrc)
                append("\n\n")
                append(layer.source)
            }.toString()

            val fsLayer = ImagineShader.compile(fsLayerSrc, ImagineShader.Type.Fragment)
                ?: return@run null

            val program = fsLayer.linkWith(vsQuad, true)
                ?: return@run null

            ImagineLayerShader(program).also {
                layerShaderMap[layer::class] = it
            }
        }
    }

    /**
     * Frees the resources associated with the construct
     */
    fun release() = releaseSafe {
        vsQuad.release()
        layerShaderMap.values.forEach { it.release() }
        layerShaderMap.clear()
        isReleased = true
    }


    /**
     * Utility function to execute the block passed only if
     * the underlying resource is not released
     *
     * @param block Lambda to be executed safely
     *
     * @return Either a valid value or null if released
     */
    private fun <T> releaseSafe(block: () -> T): T? = if (!isReleased) block() else null

    companion object {

        /**
         * Safely allocates the required resources to create an instance of
         * [ImagineLayerShaderFactory]
         *
         * @return An instance of [ImagineLayerShaderFactory] or null if error occurred
         */
        fun create(context: Context): ImagineLayerShaderFactory? {
            val vsQuadSrc = context.readRawRes(R.raw.quad)
            val fsLayerCopySrc = context.readRawRes(R.raw.layer_copy)
            val fsLayerBaseSrc = context.readRawRes(R.raw.layer_base)

            val vsQuad = ImagineShader.compile(vsQuadSrc, ImagineShader.Type.Vertex)
                ?: return null
            val fsLayerCopy = ImagineShader.compile(fsLayerCopySrc, ImagineShader.Type.Fragment)
                ?: return null

            val layerCopyShader = fsLayerCopy.linkWith(vsQuad, releaseOnFailure = true)
                ?: run {
                    vsQuad.release()
                    return null
                }

            return ImagineLayerShaderFactory(
                ImagineLayerShader(layerCopyShader),
                vsQuad,
                fsLayerBaseSrc,
            )
        }

    }

}