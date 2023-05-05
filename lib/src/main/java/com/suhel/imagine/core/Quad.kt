package com.suhel.imagine.core

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Quad private constructor(
    private val vboHandle: Int,
    private val iboHandle: Int,
    private val elementCount: Int,
) {

    private var isReleased: Boolean = false

    fun draw() = releaseSafe {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboHandle)

        GLES20.glVertexAttribPointer(
            LayerShader.aPosition,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            Float.SIZE_BYTES * COORDS_PER_VERTEX * 2,
            0,
        )
        GLES20.glEnableVertexAttribArray(LayerShader.aPosition)

        GLES20.glVertexAttribPointer(
            LayerShader.aTexCoords,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            Float.SIZE_BYTES * COORDS_PER_VERTEX * 2,
            Float.SIZE_BYTES * COORDS_PER_VERTEX,
        )
        GLES20.glEnableVertexAttribArray(LayerShader.aTexCoords)

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboHandle)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            elementCount,
            GLES20.GL_UNSIGNED_SHORT,
            0,
        )

        GLES20.glDisableVertexAttribArray(LayerShader.aTexCoords)
        GLES20.glDisableVertexAttribArray(LayerShader.aPosition)
    }

    fun release() = releaseSafe {
        val handles = intArrayOf(vboHandle, iboHandle)
        GLES20.glDeleteBuffers(handles.size, handles, 0)

        isReleased = false
    }

    private fun releaseSafe(block: () -> Unit) {
        if (!isReleased) block()
    }

    companion object {

        private const val COORDS_PER_VERTEX = 2

        fun create(): Quad {
            val vertices = floatArrayOf(
                -1.0f, 1.0f,
                0.0f, 1.0f,

                -1.0f, -1.0f,
                0.0f, 0.0f,

                1.0f, -1.0f,
                1.0f, 0.0f,

                1.0f, 1.0f,
                1.0f, 1.0f,
            )

            val indices = shortArrayOf(
                0, 1, 2,
                0, 2, 3,
            )

            val bufferArray = IntArray(2)
            GLES20.glGenBuffers(bufferArray.size, bufferArray, 0)

            val vboBuffer = ByteBuffer.allocateDirect(Float.SIZE_BYTES * vertices.size)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices)
                .position(0)

            val vboHandle = bufferArray[0]
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboHandle)
            GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                Float.SIZE_BYTES * vertices.size,
                vboBuffer,
                GLES20.GL_STATIC_DRAW
            )

            val iboBuffer = ByteBuffer.allocateDirect(Short.SIZE_BYTES * indices.size)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(indices)
                .position(0)

            val iboHandle = bufferArray[1]
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboHandle)
            GLES20.glBufferData(
                GLES20.GL_ELEMENT_ARRAY_BUFFER,
                Short.SIZE_BYTES * indices.size,
                iboBuffer,
                GLES20.GL_STATIC_DRAW
            )

            return Quad(vboHandle, iboHandle, indices.size)
        }

    }

}