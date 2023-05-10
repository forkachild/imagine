package com.suhel.imagine.core.objects

import android.opengl.GLES20
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.suhel.imagine.core.objects.ImagineShader.Compiled
import com.suhel.imagine.core.objects.ImagineShader.Program
import com.suhel.imagine.util.getProxyInt

/**
 * An abstraction for OpenGL Shaders and Shader Programs.
 *
 * This has 2 variants
 *  - [Compiled] which wraps a compiled shader
 *  - [Program] which is a linked shader capable of usage
 *
 * Create instances of [Compiled] using the companion function [compile]
 */
sealed class ImagineShader {

    /**
     * Indicates whether this underlying resource is released
     */
    protected var isReleased: Boolean = false

    /**
     * Utility function to execute the block passed only if
     * the underlying resource is not released
     *
     * @param block Lambda to be executed safely
     *
     * @return A valid value returned from the lambda or null if released
     */
    protected fun <T> releaseSafe(block: () -> T?): T? = if (isReleased) null else block()

    /**
     * Releases all resources associated with this construct
     */
    abstract fun release()

    /**
     * Wraps and represents a compiled but not linked OpenGL shader
     *
     * @property shader Handle to the underlying shader
     * @property type The type of shader it represents
     */
    class Compiled @VisibleForTesting constructor(
        private val shader: Int,
        private val type: Type
    ) : ImagineShader() {

        /**
         * Tries to link this compiled shader with another compiled shader
         *
         * @param other The other [ImagineShader.Compiled] to link with
         * @param releaseOnFailure Whether this shader should be deleted if the linking fails
         *
         * @return Either a valid [ImagineShader.Program] or null if failed
         */
        fun linkWith(other: Compiled, releaseOnFailure: Boolean = false): Program? = releaseSafe {
            // Can't link with itself or a similar kind of shader
            if (this == other || this.type == other.type) return@releaseSafe null

            // Create a shader program
            val program = GLES20.glCreateProgram()

            // Attach this shader
            GLES20.glAttachShader(program, this.shader)

            // Attach the other shader
            GLES20.glAttachShader(program, other.shader)

            // Try linking the program
            GLES20.glLinkProgram(program)

            // Extract link status
            val linkStatus = getProxyInt {
                GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, it, 0)
            }

            // Handle link error
            if (linkStatus != GLES20.GL_TRUE) {
                val error = GLES20.glGetProgramInfoLog(program)

                // Useless leaving this program in memory
                GLES20.glDeleteProgram(program)

                // Conditionally also release this compiled shader from memory
                if (releaseOnFailure) {
                    GLES20.glDeleteShader(shader)
                    isReleased = true
                }
                Log.e(TAG, "Program linking failed: $error")
                return@releaseSafe null
            }

            Program(program)
        }

        /**
         * Release the shader
         */
        override fun release() {
            releaseSafe {
                GLES20.glDeleteShader(shader)
                isReleased = true
            }
        }

    }

    /**
     * Wraps and represents a linked OpenGL shader program
     *
     * @property program Handle to the underlying shader program
     */
    class Program @VisibleForTesting constructor(
        private val program: Int
    ) : ImagineShader() {

        /**
         * Release the shader program
         */
        override fun release() {
            releaseSafe {
                GLES20.glDeleteProgram(program)
                isReleased = true
            }
        }

        /**
         * Bind this shader program to the current context
         */
        fun use() = releaseSafe {
            GLES20.glUseProgram(program)
        }

    }

    /**
     * Represents the type of shader
     */
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

        fun compile(source: String, type: Type): Compiled? {
            // Create a shader of the mentioned type
            val shader = GLES20.glCreateShader(type.rawValue)

            // Attach the source code
            GLES20.glShaderSource(shader, source)

            // Try compiling
            GLES20.glCompileShader(shader)

            // Extract compile status
            val compileStatus = getProxyInt {
                GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, it, 0)
            }

            // Handle compile error
            if (compileStatus != GLES20.GL_TRUE) {
                val error = GLES20.glGetShaderInfoLog(shader)

                // Useless leaving this shader in memory
                GLES20.glDeleteShader(shader)
                Log.e(TAG, "Shader compilation error: $error")
                return null
            }

            return Compiled(shader, type)
        }

    }

}
