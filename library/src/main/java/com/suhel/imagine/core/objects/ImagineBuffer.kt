package com.suhel.imagine.core.objects

import android.opengl.GLES20
import androidx.annotation.VisibleForTesting
import com.suhel.imagine.util.getProxyInt
import com.suhel.imagine.util.setProxyInt
import java.nio.ByteBuffer
import java.nio.ByteOrder

sealed class ImagineBuffer @VisibleForTesting constructor(val handle: Int) {

    protected var isReleased: Boolean = false

    protected fun <T> releaseSafe(block: () -> T?): T? = if (isReleased) null else block()

    fun release() = releaseSafe {
        setProxyInt(handle) {
            GLES20.glDeleteBuffers(1, it, 0)
        }

        isReleased = true
    }

    abstract fun bind()

    class Vertex(handle: Int) : ImagineBuffer(handle) {

        override fun bind() {
            releaseSafe {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, handle)
            }
        }

    }

    class Index(handle: Int) : ImagineBuffer(handle) {

        override fun bind() {
            releaseSafe {
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, handle)
            }
        }

    }

    companion object {

        fun createVertex(vertices: FloatArray): Vertex {
            val handle = getProxyInt {
                GLES20.glGenBuffers(1, it, 0)
            }

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

        fun createIndex(indices: ShortArray): Index {
            val handle = getProxyInt {
                GLES20.glGenBuffers(1, it, 0)
            }

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