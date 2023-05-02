package com.suhel.imagine.types

import android.opengl.Matrix

class Mat4 private constructor() {

    val values = FloatArray(16)

    init {
        Matrix.setIdentityM(values, 0)
    }

    fun unit(): Mat4 {
        Matrix.setIdentityM(values, 0)
        return this
    }

    fun scale(x: Float, y: Float, z: Float): Mat4 {
        Matrix.scaleM(values, 0, x, y, z)
        return this
    }

    companion object {

        fun ofUnit(): Mat4 {
            return Mat4()
        }

        fun ofScale(x: Float, y: Float, z: Float): Mat4 {
            return Mat4().scale(x, y, z)
        }

        fun ofAspectFit(
            imageAspectRatio: Float,
            containerAspectRatio: Float
        ): Mat4 = Mat4().scale(
            if (imageAspectRatio > containerAspectRatio)
                1.0f else (imageAspectRatio / containerAspectRatio),
            if (imageAspectRatio < containerAspectRatio)
                1.0f else (containerAspectRatio / imageAspectRatio),
            1.0f,
        )

    }

}