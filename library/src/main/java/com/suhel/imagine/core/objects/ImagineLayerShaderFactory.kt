package com.suhel.imagine.core.objects

import com.suhel.imagine.core.types.ImagineLayer
import kotlin.reflect.KClass

internal class ImagineLayerShaderFactory private constructor(
    val bypassShader: ImagineLayerShader,
    private val vsQuad: ImagineShader.Partial,
) {
    private var isReleased: Boolean = false
    private val layerShaderMap: MutableMap<KClass<out ImagineLayer>, ImagineLayerShader> =
        mutableMapOf()

    fun getLayerShader(layer: ImagineLayer): ImagineLayerShader? = safeCall {
        layerShaderMap[layer::class] ?: run {
            val source = "$FS_LAYER_SOURCE_HEADER\n\n${layer.source}\n\n$FS_LAYER_SOURCE_FOOTER"
            val program = ImagineShader.compile(source, ImagineShader.Type.Fragment)
                ?.linkWith(vsQuad, true) ?: return@run null

            ImagineLayerShader(program).also {
                layerShaderMap[layer::class] = it
            }
        }
    }

    fun release() = safeCall {
        vsQuad.release()
        layerShaderMap.values.forEach { it.release() }
        layerShaderMap.clear()
        isReleased = true
    }

    private fun <T> safeCall(block: () -> T): T? = if (!isReleased) block() else null

    companion object {
        private const val TAG = "ShaderFactory"

        private val VS_QUAD_SOURCE = """
            #version 300 es
            
            layout (location = 0) in vec2 aPosition;
            layout (location = 1) in vec2 aTexCoords;
            
            layout (location = 0) uniform mat4 uAspectRatio;
            layout (location = 1) uniform mat4 uInvert;
            
            out vec2 vTexCoords;
            
            void main() {
                gl_Position = uAspectRatio * uInvert * vec4(aPosition, 0.0, 1.0);
                vTexCoords = aTexCoords;
            }
        """.trimIndent()

        private val FS_LAYER_SOURCE_HEADER = """
            #version 300 es
            precision mediump float;
            
            in vec2 vTexCoords;
            
            layout (location = 2) uniform sampler2D uImage;
            layout (location = 3) uniform float uIntensity;
            
            out vec4 fragColor;
        """.trimIndent()

        private val FS_LAYER_SOURCE_FOOTER = """
            void main() {
                vec4 originalColor = texture(uImage, vTexCoords);
                vec4 processedColor = process(originalColor);
                fragColor = (uIntensity * processedColor) + ((1.0 - uIntensity) * originalColor);
            }
        """.trimIndent()

        private val FS_COPY_SOURCE = """
            #version 300 es
            precision mediump float;
            
            in vec2 vTexCoords;
            
            layout (location = 2) uniform sampler2D uImage;
            
            out vec4 fragColor;
            
            void main() {
                fragColor = texture(uImage, vTexCoords);
            }
        """.trimIndent()

        fun create(): ImagineLayerShaderFactory? {
            val vsQuad = ImagineShader.compile(VS_QUAD_SOURCE, ImagineShader.Type.Vertex) ?: return null
            val copyShader = ImagineShader.compile(FS_COPY_SOURCE, ImagineShader.Type.Fragment)
                ?.linkWith(vsQuad, true) ?: return null

            return ImagineLayerShaderFactory(ImagineLayerShader(copyShader), vsQuad)
        }

    }

}