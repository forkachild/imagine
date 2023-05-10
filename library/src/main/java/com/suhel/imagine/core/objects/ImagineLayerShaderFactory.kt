package com.suhel.imagine.core.objects

import androidx.annotation.VisibleForTesting
import com.suhel.imagine.core.types.ImagineLayer
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
            val source = "$FS_LAYER_SOURCE_BASE\n\n${layer.source}"
            val program = ImagineShader.compile(source, ImagineShader.Type.Fragment)
                ?.linkWith(vsQuad, true) ?: return@run null

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
         * The static source code of Vertex Shader used in Quad rendering
         */
        private val VS_QUAD_SOURCE = """
            #version 300 es
            
            // Binding point for the vertex position attribute
            layout (location = 0) in vec2 aPosition;
            
            // Binding point for the texture coordinates attribute
            layout (location = 1) in vec2 aTexCoords;
            
            // Binding point for the aspect ratio matrix uniform
            layout (location = 0) uniform mat4 uAspectRatio;
            
            // Binding point for the invert matrix uniform
            layout (location = 1) uniform mat4 uInvert;
            
            // Binding point for interpolated texture coordinates
            // output to the fragment shader
            layout (location = 0) out vec2 vTexCoords;
            
            // Entry point invoked for every vertex
            void main() {
                // Transform the vertex coordinates by inverting them first
                // and then applying aspect ratio correction
                gl_Position = uAspectRatio * uInvert * vec4(aPosition, 0.0, 1.0);
                
                // Pass through the texture coordinates by interpolating them
                vTexCoords = aTexCoords;
            }
        """.trimIndent()

        /**
         * The static base source code of the Fragment Shader for rendering layers.
         * It is incomplete unless `vec4 process(vec4 color)` is defined
         */
        private val FS_LAYER_SOURCE_BASE = """
            #version 300 es
            
            // Precision is limited to "medium". Other values are
            // "highp" and "lowp" where the "p" stands for precision
            precision mediump float;
            
            // Binding point for the texture coordinates interpolated from
            // the vertex shader
            layout (location = 0) in vec2 vTexCoords;
            
            // Binding point for the image texture uniform
            layout (location = 2) uniform sampler2D uImage;
            
            // Binding point for the intensity float uniform
            layout (location = 3) uniform float uIntensity;
            
            // Binding point for the color output for the current fragment
            layout (location = 0) out vec4 fragColor;
            
            // Declare the abstract function that needs to be implemented
            // by the 
            vec4 process(vec4 color);
            
            // Entry point invoked for every fragment
            void main() {
                // Sample the original pixel color from the image texture
                vec4 originalColor = texture(uImage, vTexCoords);
                
                // Pass it down to the abstract processor
                vec4 processedColor = process(originalColor);
                
                // Blend the original and processed color
                fragColor = (uIntensity * processedColor) + ((1.0 - uIntensity) * originalColor);
            }
        """.trimIndent()

        /**
         * The static source code for the Fragment Shader used to return
         * unprocessed pixel values from a texture
         */
        private val FS_COPY_SOURCE = """
            #version 300 es
            
            // Precision is limited to "mediump". Other values are
            // "highp" and "lowp" where the "p" stands for precision
            precision mediump float;
            
            // Binding point for the texture coordinates interpolated from
            // the vertex shader
            layout (location = 0) in vec2 vTexCoords;
            
            // Binding point for the image texture uniform
            layout (location = 2) uniform sampler2D uImage;
            
            // Binding point for the color output for the current fragment
            layout (location = 0) out vec4 fragColor;
            
            // Entry point invoked for every fragment
            void main() {
                // Pass through the sampled pixel color
                fragColor = texture(uImage, vTexCoords);
            }
        """.trimIndent()

        /**
         * Safely allocates the required resources to create an instance of
         * [ImagineLayerShaderFactory]
         *
         * @return An instance of [ImagineLayerShaderFactory] or null if error occurred
         */
        fun create(): ImagineLayerShaderFactory? {
            val vsQuad = ImagineShader.compile(VS_QUAD_SOURCE, ImagineShader.Type.Vertex) ?: return null
            val copyShader = ImagineShader.compile(FS_COPY_SOURCE, ImagineShader.Type.Fragment)
                ?.linkWith(vsQuad, true) ?: return null

            return ImagineLayerShaderFactory(ImagineLayerShader(copyShader), vsQuad)
        }

    }

}