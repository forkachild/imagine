package com.suhel.imagine.core.objects

import android.opengl.GLES20
import androidx.annotation.VisibleForTesting

/**
 * An abstraction to render a Quad using OpenGL
 *
 * @property vbo A valid [ImagineBuffer.Vertex] holding vertex data
 * @property ibo A valid [ImagineBuffer.Index] holding index data
 * @property elementCount Number of indices
 */
internal class ImagineQuad @VisibleForTesting constructor(
    private val vbo: ImagineBuffer.Vertex,
    private val ibo: ImagineBuffer.Index,
    private val elementCount: Int,
) {

    /**
     * Indicates whether this underlying resource is released
     */
    private var isReleased: Boolean = false

    /**
     * Draws the quad into the currently bound framebuffer
     */
    fun draw() = releaseSafe {
        // Required to bind the vertex buffer before attributes can be bound
        vbo.bind()

        // Bind vertex position attribute
        ImagineLayerShader.position.bindVBO(
            COORDS_PER_VERTEX,
            Float.SIZE_BYTES * COORDS_PER_VERTEX * 2,
            0,
        )

        // Need to enable the vertex position attribute for streaming data
        ImagineLayerShader.position.enable()

        // Bind texture coordinates attribute
        ImagineLayerShader.texCoords.bindVBO(
            COORDS_PER_VERTEX,
            Float.SIZE_BYTES * COORDS_PER_VERTEX * 2,
            Float.SIZE_BYTES * COORDS_PER_VERTEX,
        )

        // Need to enable the texture position attribute for streaming data
        ImagineLayerShader.texCoords.enable()

        // Bind the index buffer for proper rendering
        ibo.bind()

        // Draw the indexed vertices
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            elementCount,
            GLES20.GL_UNSIGNED_SHORT,
            0,
        )

        // Disable the attributes since data streaming is done
        ImagineLayerShader.texCoords.disable()
        ImagineLayerShader.position.disable()
    }

    /**
     * Releases all resources associated with this construct
     */
    fun release() = releaseSafe {
        ibo.release()
        vbo.release()

        isReleased = true
    }

    /**
     * Utility function to execute the block passed only if
     * the underlying resource is not released
     *
     * @param block Lambda to be executed safely
     */
    private fun releaseSafe(block: () -> Unit) {
        if (!isReleased) block()
    }

    companion object {

        /**
         * Number of coordinates representing each vertex. Since this
         * is essentially a 2D quad, so 2 coordinates are required.
         */
        private const val COORDS_PER_VERTEX = 2

        /**
         * Interleaved vertex coordinates followed by corresponding
         * texture coordinates, representing a quad
         */
        private val vertices = floatArrayOf(
            // Top left
            -1.0f, 1.0f,
            0.0f, 1.0f,

            // Bottom left
            -1.0f, -1.0f,
            0.0f, 0.0f,

            // Bottom right
            1.0f, -1.0f,
            1.0f, 0.0f,

            // Top right
            1.0f, 1.0f,
            1.0f, 1.0f,
        )

        /**
         * Indices into the vertex buffer to describe two anti-clockwise
         * triangles to be rendered representing a quad
         */
        private val indices = shortArrayOf(
            // First triangle
            0, 1, 2,

            // Second triangle
            0, 2, 3,
        )

        /**
         * Safely allocate and return an instance of [ImagineQuad]
         *
         * @return An instance of [ImagineQuad]
         */
        fun create(): ImagineQuad = ImagineQuad(
            ImagineBuffer.createVertex(vertices),
            ImagineBuffer.createIndex(indices),
            indices.size
        )

    }

}