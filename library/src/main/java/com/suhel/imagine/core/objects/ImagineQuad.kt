package com.suhel.imagine.core.objects

import android.opengl.GLES20

internal class ImagineQuad private constructor(
    private val vbo: ImagineBuffer.Vertex,
    private val ibo: ImagineBuffer.Index,
    private val elementCount: Int,
) {

    private var isReleased: Boolean = false

    fun draw() = releaseSafe {
        vbo.bind()
        ImagineLayerShader.position.bindVBO(
            COORDS_PER_VERTEX,
            Float.SIZE_BYTES * COORDS_PER_VERTEX * 2,
            0,
        )
        ImagineLayerShader.position.enable()

        ImagineLayerShader.texCoords.bindVBO(
            COORDS_PER_VERTEX,
            Float.SIZE_BYTES * COORDS_PER_VERTEX * 2,
            Float.SIZE_BYTES * COORDS_PER_VERTEX,
        )
        ImagineLayerShader.texCoords.enable()

        ibo.bind()
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            elementCount,
            GLES20.GL_UNSIGNED_SHORT,
            0,
        )

        ImagineLayerShader.texCoords.disable()
        ImagineLayerShader.position.disable()
    }

    fun release() = releaseSafe {
        ibo.release()
        vbo.release()

        isReleased = true
    }

    private fun releaseSafe(block: () -> Unit) {
        if (!isReleased) block()
    }

    companion object {

        private const val COORDS_PER_VERTEX = 2

        fun create(): ImagineQuad {
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
            val vbo = ImagineBuffer.createVertex(vertices)

            val indices = shortArrayOf(
                0, 1, 2,
                0, 2, 3,
            )
            val ibo = ImagineBuffer.createIndex(indices)

            return ImagineQuad(vbo, ibo, indices.size)
        }

    }

}