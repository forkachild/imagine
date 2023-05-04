package com.suhel.imagine.core

import com.suhel.imagine.types.Layer
import kotlin.reflect.KClass

class LayerShaderFactory private constructor(
    val bypassShader: LayerShader.Bypass,
    private val vsQuad: Shader.Partial,
) {
    private var isReleased: Boolean = false
    private val layerShaderMap: MutableMap<KClass<out Layer>, LayerShader.Layer> =
        mutableMapOf()

    fun getLayerShader(layer: Layer): LayerShader.Layer? = safeCall {
        layerShaderMap[layer::class] ?: run {
            val source = "$FS_LAYER_SOURCE_HEADER\n\n${layer.source}\n\n$FS_LAYER_SOURCE_FOOTER"
            val program = Shader
                .compile(source, Shader.Type.Fragment)
                ?.linkWith(vsQuad, true) ?: return@run null

            LayerShader.Layer(program).also {
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

        fun create(): LayerShaderFactory? {
            val vsQuad = Shader.compile(VS_QUAD_SOURCE, Shader.Type.Vertex) ?: return null
            val copyShader = Shader
                .compile(FS_COPY_SOURCE, Shader.Type.Fragment)
                ?.linkWith(vsQuad, true) ?: return null

            return LayerShaderFactory(LayerShader.Bypass(copyShader), vsQuad)
        }

    }

}