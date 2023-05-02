package com.suhel.imagine.core

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Quad private constructor(
    private val vboHandle: Int,
    private val iboHandle: Int,
    private val elementCount: Int,
) {

    private var isReleased: Boolean = false

    fun draw() = safeCall {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboHandle)

        GLES30.glVertexAttribPointer(
            Shader.aPosition,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            Float.SIZE_BYTES * COORDS_PER_VERTEX * 2,
            0,
        )
        GLES30.glEnableVertexAttribArray(Shader.aPosition)

        GLES30.glVertexAttribPointer(
            Shader.aTexCoords,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            Float.SIZE_BYTES * COORDS_PER_VERTEX * 2,
            Float.SIZE_BYTES * COORDS_PER_VERTEX,
        )
        GLES30.glEnableVertexAttribArray(Shader.aTexCoords)

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, iboHandle)
        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES,
            elementCount,
            GLES30.GL_UNSIGNED_SHORT,
            0,
        )

        GLES30.glDisableVertexAttribArray(Shader.aTexCoords)
        GLES30.glDisableVertexAttribArray(Shader.aPosition)
    }

    fun release() = safeCall {
        val handles = intArrayOf(vboHandle, iboHandle)
        GLES30.glDeleteBuffers(handles.size, handles, 0)

        isReleased = false
    }

    private fun safeCall(block: () -> Unit) {
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
            GLES30.glGenBuffers(bufferArray.size, bufferArray, 0)

            val vboBuffer = ByteBuffer.allocateDirect(Float.SIZE_BYTES * vertices.size)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices)
                .position(0)

            val vboHandle = bufferArray[0]
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboHandle)
            GLES30.glBufferData(
                GLES30.GL_ARRAY_BUFFER,
                Float.SIZE_BYTES * vertices.size,
                vboBuffer,
                GLES30.GL_STATIC_DRAW
            )

            val iboBuffer = ByteBuffer.allocateDirect(Short.SIZE_BYTES * indices.size)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(indices)
                .position(0)

            val iboHandle = bufferArray[1]
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, iboHandle)
            GLES30.glBufferData(
                GLES30.GL_ELEMENT_ARRAY_BUFFER,
                Short.SIZE_BYTES * indices.size,
                iboBuffer,
                GLES30.GL_STATIC_DRAW
            )

            return Quad(vboHandle, iboHandle, indices.size)
        }

    }

}