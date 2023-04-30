package com.suhel.imagine.util

import android.opengl.GLES30
import com.suhel.imagine.Constants
import com.suhel.imagine.core.Shader
import com.suhel.imagine.types.Layer

class ShaderFactory {

    private var vsHandle: Int = Constants.Resources.INVALID_HANDLE

    private var isAllocated: Boolean = false

    fun allocate() {
        vsHandle = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        GLES30.glShaderSource(vsHandle, VS_SOURCE)
        GLES30.glCompileShader(vsHandle)
        val compileStatus = getProxyInt {
            GLES30.glGetShaderiv(vsHandle, GLES30.GL_COMPILE_STATUS, it, 0)
        }
        if (compileStatus != GLES30.GL_TRUE) {
            val error = GLES30.glGetShaderInfoLog(vsHandle)
            GLES30.glDeleteShader(vsHandle)
            throw IllegalStateException("VS Error: $error")
        }

        isAllocated = true
    }

    fun generate(layer: Layer): Shader {
        checkAllocated()

        val fsHandle = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(fsHandle, generateSource(layer.source))
        GLES30.glCompileShader(fsHandle)
        val compileStatus = getProxyInt {
            GLES30.glGetShaderiv(fsHandle, GLES30.GL_COMPILE_STATUS, it, 0)
        }
        if (compileStatus != GLES30.GL_TRUE) {
            val error = GLES30.glGetShaderInfoLog(fsHandle)
            GLES30.glDeleteShader(fsHandle)
            throw IllegalStateException("FS Error: $error")
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
            throw IllegalStateException("Program Error: $error")
        }

        return Shader(programHandle)
    }

    fun release() {
        checkAllocated()
        GLES30.glDeleteShader(vsHandle)
    }

    private fun checkAllocated() {
        if(!isAllocated) throw IllegalStateException("EffectShaderFactory not allocated")
    }

    companion object {
        private val VS_SOURCE = """
            #version 300 es
            
            layout (location = 0) in vec2 aPosition;
            layout (location = 1) in vec2 aTexCoord;
            
            layout (location = 0) uniform mat4 uAspectRatio;
            layout (location = 1) uniform mat4 uInvert;
            
            out vec2 vTexCoord;
            
            void main() {
                gl_Position = uAspectRatio * uInvert * vec4(aPosition, 0.0, 1.0);
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        private val FS_SOURCE_HEADER = """
            #version 300 es
            precision mediump float;
            
            in vec2 vTexCoord;
            
            layout (location = 2) uniform sampler2D uImage;
            layout (location = 3) uniform float uFactor;
            
            out vec4 fragColor;
        """.trimIndent()

        private val FS_SOURCE_FOOTER = """
            void main() {
                vec4 originalColor = texture(uImage, vTexCoord);
                vec4 processedColor = process(originalColor);
                fragColor = (uFactor * processedColor) + ((1.0 - uFactor) * originalColor);
            }
        """.trimIndent()

        private fun generateSource(effectSource: String): String {
            return "$FS_SOURCE_HEADER\n\n$effectSource\n\n$FS_SOURCE_FOOTER"
        }

    }

}