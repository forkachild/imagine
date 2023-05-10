package com.suhel.imagine.core.objects

import android.opengl.GLES20
import androidx.annotation.VisibleForTesting
import com.suhel.imagine.core.objects.ImagineBuffer.Index
import com.suhel.imagine.core.objects.ImagineBuffer.Vertex
import com.suhel.imagine.util.getProxyInt
import com.suhel.imagine.util.setProxyInt
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * A wrapper for an OpenGL Buffer Object with allocation safety.
 * Being a sealed class, it has 2 variants, [Vertex] and [Index] to store
 * [GLES20.GL_ARRAY_BUFFER] and [GLES20.GL_ELEMENT_ARRAY_BUFFER] respectively
 *
 * @property handle Reference to the underlying Buffer Object
 */
internal sealed class ImagineBuffer @VisibleForTesting constructor(
    protected val handle: Int
) {

    /**
     * Indicates whether this underlying resource is released
     */
    private var isReleased: Boolean = false

    /**
     * Utility function to execute the block passed only if
     * the underlying resource is not released
     *
     * @param block Lambda to be executed safely
     */
    protected fun releaseSafe(block: () -> Unit) {
        if (!isReleased) block()
    }

    /**
     * Release the underlying Buffer Object from memory
     */
    fun release() = releaseSafe {
        setProxyInt(handle) {
            GLES20.glDeleteBuffers(1, it, 0)
        }

        isReleased = true
    }

    /**
     * Bind this Buffer Object during drawing.
     * Implemented in the variants
     */
    abstract fun bind()

    /**
     * The Vertex Buffer Object variant
     *
     * @param handle The handle to pass on to the base class
     *
     * @see [GLES20.GL_ARRAY_BUFFER]
     */
    class Vertex(handle: Int) : ImagineBuffer(handle) {

        override fun bind() = releaseSafe {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, handle)
        }

    }

    /**
     * The Index Buffer Object variant
     *
     * @param handle The handle to pass on to the base class
     *
     * @see [GLES20.GL_ELEMENT_ARRAY_BUFFER]
     */
    class Index(handle: Int) : ImagineBuffer(handle) {

        override fun bind() = releaseSafe {
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, handle)
        }

    }

    companion object {

        /**
         * Safely allocate an OpenGL [GLES20.GL_ARRAY_BUFFER] from a given
         * set of vertices and wrap it in [Vertex] variant
         *
         * @param vertices Float vertex data to fill the buffer with
         *
         * @return An active [Vertex]
         */
        fun createVertex(vertices: FloatArray): Vertex {
            val handle = getProxyInt {
                GLES20.glGenBuffers(1, it, 0)
            }

            // Create a directly allocated Buffer in memory
            // and put the vertex data in it
            val buffer = ByteBuffer.allocateDirect(Float.SIZE_BYTES * vertices.size)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices)
                .position(0)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, handle)
            GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                Float.SIZE_BYTES * vertices.size,
                buffer,
                GLES20.GL_STATIC_DRAW
            )

            return Vertex(handle)
        }


        /**
         * Safely allocate an OpenGL [GLES20.GL_ELEMENT_ARRAY_BUFFER] from a given
         * set of indices and wrap it in [Index] variant
         *
         * @param indices Short index data to fill the buffer with
         *
         * @return An active [Index]
         */
        fun createIndex(indices: ShortArray): Index {
            val handle = getProxyInt {
                GLES20.glGenBuffers(1, it, 0)
            }

            // Create a directly allocated Buffer in memory
            // and put the index data in it
            val buffer = ByteBuffer.allocateDirect(Short.SIZE_BYTES * indices.size)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(indices)
                .position(0)

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, handle)
            GLES20.glBufferData(
                GLES20.GL_ELEMENT_ARRAY_BUFFER,
                Short.SIZE_BYTES * indices.size,
                buffer,
                GLES20.GL_STATIC_DRAW
            )

            return Index(handle)
        }

    }

}