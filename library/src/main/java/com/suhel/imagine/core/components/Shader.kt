package com.suhel.imagine.core.components

import android.opengl.GLES20
import android.util.Log
import com.suhel.imagine.util.getProxyInt

internal sealed class Shader {

    protected var isReleased: Boolean = false

    protected fun <T> releaseSafe(block: () -> T?): T? = if (isReleased) null else block()

    abstract fun release()

    class Partial(
        private val shader: Int,
        private val type: Type
    ) : Shader() {

        fun linkWith(other: Partial, releaseOnFailure: Boolean = false): Complete? = releaseSafe {
            if (this == other || this.type == other.type) return@releaseSafe null

            val program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, this.shader)
            GLES20.glAttachShader(program, other.shader)
            GLES20.glLinkProgram(program)
            val linkStatus = getProxyInt {
                GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, it, 0)
            }
            if (linkStatus != GLES20.GL_TRUE) {
                val error = GLES20.glGetProgramInfoLog(program)
                GLES20.glDeleteProgram(program)
                if (releaseOnFailure) {
                    GLES20.glDeleteShader(shader)
                    isReleased = true
                }
                Log.e(TAG, "Program linking failed: $error")
                return@releaseSafe null
            }

            Complete(program)
        }

        override fun release() {
            releaseSafe {
                GLES20.glDeleteShader(shader)
                isReleased = true
            }
        }

    }

    class Complete(val program: Int) : Shader() {

        override fun release() {
            releaseSafe {
                GLES20.glDeleteProgram(program)
                isReleased = true
            }
        }

        fun use() = releaseSafe {
            GLES20.glUseProgram(program)
        }

    }

    enum class Type {
        Vertex,
        Fragment;

        val rawValue: Int
            get() = when (this) {
                Vertex -> GLES20.GL_VERTEX_SHADER
                Fragment -> GLES20.GL_FRAGMENT_SHADER
            }
    }

    companion object {

        private const val TAG = "Shader"

        fun compile(source: String, type: Type): Partial? {
            val shader = GLES20.glCreateShader(type.rawValue)
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compileStatus = getProxyInt {
                GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, it, 0)
            }
            if (compileStatus != GLES20.GL_TRUE) {
                val error = GLES20.glGetShaderInfoLog(shader)
                GLES20.glDeleteShader(shader)
                Log.e(TAG, "Shader compilation error: $error")
                return null
            }

            return Partial(shader, type)
        }

    }

}
