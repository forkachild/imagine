package com.suhel.imagine.core

import android.opengl.GLES30
import android.util.Log
import com.suhel.imagine.util.getProxyInt

sealed class Shader {

    protected var isReleased: Boolean = false

    protected fun <T> releaseSafe(block: () -> T?): T? = if (isReleased) null else block()

    abstract fun release()

    class Partial(
        private val shader: Int,
        private val type: Type
    ) : Shader() {

        fun linkWith(other: Partial, releaseOnFailure: Boolean = false): Complete? = releaseSafe {
            if (this == other || this.type == other.type) return@releaseSafe null

            val program = GLES30.glCreateProgram()
            GLES30.glAttachShader(program, this.shader)
            GLES30.glAttachShader(program, other.shader)
            GLES30.glLinkProgram(program)
            val linkStatus = getProxyInt {
                GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, it, 0)
            }
            if (linkStatus != GLES30.GL_TRUE) {
                val error = GLES30.glGetProgramInfoLog(program)
                GLES30.glDeleteProgram(program)
                if (releaseOnFailure) {
                    GLES30.glDeleteShader(shader)
                    isReleased = true
                }
                Log.e(TAG, "Program linking failed: $error")
                return@releaseSafe null
            }

            Complete(program)
        }

        override fun release() {
            releaseSafe {
                GLES30.glDeleteShader(shader)
                isReleased = true
            }
        }

    }

    class Complete(val program: Int) : Shader() {

        override fun release() {
            releaseSafe {
                GLES30.glDeleteProgram(program)
                isReleased = true
            }
        }

        fun use() = releaseSafe {
            GLES30.glUseProgram(program)
        }

    }

    enum class Type {
        Vertex,
        Fragment;

        val rawValue: Int
            get() = when (this) {
                Vertex -> GLES30.GL_VERTEX_SHADER
                Fragment -> GLES30.GL_FRAGMENT_SHADER
            }
    }

    companion object {

        private const val TAG = "Shader"

        fun compile(source: String, type: Type): Partial? {
            val shader = GLES30.glCreateShader(type.rawValue)
            GLES30.glShaderSource(shader, source)
            GLES30.glCompileShader(shader)
            val compileStatus = getProxyInt {
                GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, it, 0)
            }
            if (compileStatus != GLES30.GL_TRUE) {
                val error = GLES30.glGetShaderInfoLog(shader)
                GLES30.glDeleteShader(shader)
                Log.e(TAG, "Shader compilation error: $error")
                return null
            }

            return Partial(shader, type)
        }

    }

}
