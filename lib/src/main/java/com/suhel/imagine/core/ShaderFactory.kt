package com.suhel.imagine.core

import android.opengl.GLES30
import android.util.Log
import com.suhel.imagine.types.Layer
import com.suhel.imagine.util.getProxyInt
import kotlin.reflect.KClass

class ShaderFactory private constructor(
    private val vsHandle: Int,
) {
    private var isReleased: Boolean = false
    private val layerShaderMap: MutableMap<KClass<out Layer>, LayerShader> = mutableMapOf()

    fun getShader(layer: Layer): LayerShader? = safeCall {
        layerShaderMap[layer::class] ?: run {
            val fsHandle = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
            GLES30.glShaderSource(fsHandle, generateSource(layer.source))
            GLES30.glCompileShader(fsHandle)
            val compileStatus = getProxyInt {
                GLES30.glGetShaderiv(fsHandle, GLES30.GL_COMPILE_STATUS, it, 0)
            }
            if (compileStatus != GLES30.GL_TRUE) {
                val error = GLES30.glGetShaderInfoLog(fsHandle)
                GLES30.glDeleteShader(fsHandle)
                Log.e(TAG, "FS error: $error")
                return@run null
            }

            val programHandle = GLES30.glCreateProgram()
            GLES30.glAttachShader(programHandle, vsHandle)
            GLES30.glAttachShader(programHandle, fsHandle)
            GLES30.glLinkProgram(programHandle)
            GLES30.glDeleteShader(fsHandle)
            val linkStatus = getProxyInt {
                GLES30.glGetProgramiv(programHandle, GLES30.GL_LINK_STATUS, it, 0)
            }
            if (linkStatus != GLES30.GL_TRUE) {
                val error = GLES30.glGetProgramInfoLog(programHandle)
                GLES30.glDeleteProgram(programHandle)
                Log.e(TAG, "Program error: $error")
                return@run null
            }

            val shader = LayerShader(programHandle)
            layerShaderMap[layer::class] = shader
            shader
        }
    }

    fun release() = safeCall {
        GLES30.glDeleteShader(vsHandle)
        layerShaderMap.values.forEach { it.release() }
        layerShaderMap.clear()
        isReleased = true
    }

    private fun <T> safeCall(block: () -> T): T? = if (!isReleased) block() else null

    companion object {
        private const val TAG = "ShaderFactory"

        private val VS_SOURCE = """
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

        private val FS_SOURCE_HEADER = """
            #version 300 es
            precision mediump float;
            
            in vec2 vTexCoords;
            
            layout (location = 2) uniform sampler2D uImage;
            layout (location = 3) uniform float uIntensity;
            
            out vec4 fragColor;
        """.trimIndent()

        private val FS_SOURCE_FOOTER = """
            void main() {
                vec4 originalColor = texture(uImage, vTexCoords);
                vec4 processedColor = process(originalColor);
                fragColor = (uIntensity * processedColor) + ((1.0 - uIntensity) * originalColor);
            }
        """.trimIndent()

        private fun generateSource(effectSource: String): String {
            return "$FS_SOURCE_HEADER\n\n$effectSource\n\n$FS_SOURCE_FOOTER"
        }

        fun create(): ShaderFactory? {
            val vsHandle = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
            GLES30.glShaderSource(vsHandle, VS_SOURCE)
            GLES30.glCompileShader(vsHandle)
            val compileStatus = getProxyInt {
                GLES30.glGetShaderiv(vsHandle, GLES30.GL_COMPILE_STATUS, it, 0)
            }
            if (compileStatus != GLES30.GL_TRUE) {
                val error = GLES30.glGetShaderInfoLog(vsHandle)
                GLES30.glDeleteShader(vsHandle)
                Log.e(TAG, "VS error: $error")
                return null
            }

            return ShaderFactory(vsHandle)
        }

    }

}