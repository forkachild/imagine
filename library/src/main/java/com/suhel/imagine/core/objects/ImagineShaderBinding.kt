package com.suhel.imagine.core.objects

import android.opengl.GLES20
import com.suhel.imagine.core.types.ImagineMatrix

internal sealed class ImagineShaderBinding(val location: Int) {

    class Uniform(location: Int) : ImagineShaderBinding(location) {

        fun bindMatrix(matrix: ImagineMatrix) {
            GLES20.glUniformMatrix4fv(location, 1, false, matrix.data, 0)
        }

        fun bindFloat(value: Float) {
            GLES20.glUniform1f(location, value)
        }

        fun bindTexture(texture: ImagineTexture, index: Int = 0) {
            GLES20.glUniform1i(location, index)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + index)
            texture.bind()
        }

    }

    class Attribute(location: Int) : ImagineShaderBinding(location) {

        fun enable() {
            GLES20.glEnableVertexAttribArray(location)
        }

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

        fun disable() {
            GLES20.glDisableVertexAttribArray(location)
        }

    }

}