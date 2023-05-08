package com.suhel.imagine.core.types

import android.opengl.Matrix

class ImagineMatrix private constructor() {

    val data = FloatArray(16)

    init {
        Matrix.setIdentityM(data, 0)
    }

    fun scale(x: Float, y: Float, z: Float): ImagineMatrix {
        Matrix.scaleM(data, 0, x, y, z)
        return this
    }

    companion object {

        fun ofUnit(): ImagineMatrix {
            return ImagineMatrix()
        }

        fun ofScale(x: Float, y: Float, z: Float): ImagineMatrix {
            return ImagineMatrix().scale(x, y, z)
        }

        fun ofAspectFit(
            imageAspectRatio: Float,
            containerAspectRatio: Float
        ): ImagineMatrix = ImagineMatrix().scale(
            if (imageAspectRatio > containerAspectRatio)
                1.0f else (imageAspectRatio / containerAspectRatio),
            if (imageAspectRatio < containerAspectRatio)
                1.0f else (containerAspectRatio / imageAspectRatio),
            1.0f,
        )

        val identity = ofUnit()
        val invertY = ofScale(1.0f, -1.0f, 1.0f)

    }

}