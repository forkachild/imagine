package com.suhel.imagine.core.objects

import android.opengl.GLES20
import com.suhel.imagine.core.objects.ImagineShaderBinding.Attribute
import com.suhel.imagine.core.objects.ImagineShaderBinding.Uniform
import com.suhel.imagine.core.types.ImagineMatrix

/**
 * Abstracts a binding into a shader program
 *
 * This has 2 variants
 * - [Uniform]
 * - [Attribute]
 *
 * @property location The location of the binding in the shader layout
 */
internal sealed class ImagineShaderBinding(protected val location: Int) {

    /**
     * Abstracts a binding to a OpenGL Shader Uniform
     *
     * @param location The location of the binding in the shader layout
     */
    class Uniform(location: Int) : ImagineShaderBinding(location) {

        /**
         * Binds a matrix to this binding point
         *
         * @param matrix The [ImagineMatrix] to bind
         */
        fun bindMatrix(matrix: ImagineMatrix) {
            GLES20.glUniformMatrix4fv(location, 1, false, matrix.data, 0)
        }

        /**
         * Binds a scalar float to this binding point
         *
         * @param value The [Float] value
         */
        fun bindFloat(value: Float) {
            GLES20.glUniform1f(location, value)
        }

        /**
         * Binds a scalar int to this binding point
         *
         * @param value The [Int] value
         */
        fun bindInt(value: Int) {
            GLES20.glUniform1i(location, value)
        }

        /**
         * Binds a texture to this sampler binding point
         *
         * @param texture The [ImagineTexture] to bind
         * @param index The texture index to use for binding. Default 0
         */
        fun bindTexture(texture: ImagineTexture, index: Int = 0) {
            bindInt(index)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + index)
            texture.bind()
        }

    }

    /**
     * Abstracts a binding to a OpenGL Shader Attribute
     *
     * @param location The location of the binding in the shader layout
     */
    class Attribute(location: Int) : ImagineShaderBinding(location) {

        /**
         * Enable the binding point as a vertex attribute array
         */
        fun enable() {
            GLES20.glEnableVertexAttribArray(location)
        }

        /**
         * Binds a bound [GLES20.GL_ARRAY_BUFFER] at this binding point
         *
         * @param size Number of components for each vertex
         * @param stride Gap in bytes between each vertex
         * @param offset Offset in bytes into the vertex buffer object
         */
        fun bindVBO(size: Int, stride: Int, offset: Int) {
            GLES20.glVertexAttribPointer(
                location,
                size,
                GLES20.GL_FLOAT,
                false,
                stride,
                offset,
            )
        }

        /**
         * Disables the binding as a vertex attribute array
         */
        fun disable() {
            GLES20.glDisableVertexAttribArray(location)
        }

    }

}