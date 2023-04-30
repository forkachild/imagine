package com.suhel.imagine.types

import android.opengl.Matrix

class Mat4 {

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

    fun translate(x: Float, y: Float, z: Float): Mat4 {
        Matrix.translateM(values, 0, x, y, z)
        return this
    }

    companion object {

        fun ofScale(x: Float, y: Float, z: Float): Mat4 {
            return Mat4().scale(x, y, z)
        }

    }

}