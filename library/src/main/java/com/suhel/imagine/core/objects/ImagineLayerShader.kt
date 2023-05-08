package com.suhel.imagine.core.objects

import com.suhel.imagine.core.types.ImagineMatrix

internal class ImagineLayerShader(val program: ImagineShader.Program) {

    fun release() {
        program.release()
    }

    fun use() {
        program.use()
    }

    fun bindAspectRatioMatrix(matrix: ImagineMatrix) {
        aspectRatio.bindMatrix(matrix)
    }

    fun bindInvertMatrix(matrix: ImagineMatrix) {
        invert.bindMatrix(matrix)
    }

    fun bindImage(texture: ImagineTexture, index: Int = 0) {
        image.bindTexture(texture, index)
    }

    fun bindIntensity(value: Float) {
        intensity.bindFloat(value)
    }

    companion object {
        val position = ImagineShaderBinding.Attribute(0)
        val texCoords = ImagineShaderBinding.Attribute(1)
        val aspectRatio = ImagineShaderBinding.Uniform(0)
        val invert = ImagineShaderBinding.Uniform(1)
        val image = ImagineShaderBinding.Uniform(2)
        val intensity = ImagineShaderBinding.Uniform(3)
    }
}