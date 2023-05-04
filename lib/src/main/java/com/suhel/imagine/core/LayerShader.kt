package com.suhel.imagine.core

import android.opengl.GLES30
import com.suhel.imagine.types.Mat4

sealed class LayerShader(private val shader: Shader.Complete) {

    val program: Int
        get() = shader.program

    fun release() {
        shader.release()
    }

    fun use() {
        shader.use()
    }

    fun setAspectRatioMatrix(matrix: Mat4) {
        GLES30.glUniformMatrix4fv(uAspectRatio, 1, false, matrix.data, 0)
    }

    fun setInvertMatrix(matrix: Mat4) {
        GLES30.glUniformMatrix4fv(uInvert, 1, false, matrix.data, 0)
    }

    fun setImage(texture: Texture, atIndex: Int = 0) {
        GLES30.glUniform1i(uImage, atIndex)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + atIndex)
        texture.bind()
    }

    class Bypass(shader: Shader.Complete) : LayerShader(shader)

    class Layer(shader: Shader.Complete) : LayerShader(shader) {

        fun setIntensity(value: Float) {
            GLES30.glUniform1f(uIntensity, value)
        }

    }

    companion object {
        const val aPosition: Int = 0
        const val aTexCoords: Int = 1
        const val uAspectRatio: Int = 0
        const val uInvert: Int = 1
        const val uImage: Int = 2
        const val uIntensity: Int = 3
    }
}