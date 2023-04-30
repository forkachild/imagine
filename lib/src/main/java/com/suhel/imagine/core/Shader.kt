package com.suhel.imagine.core

import android.opengl.GLES30

class Shader(val program: Int) {

    fun use() {
        GLES30.glUseProgram(program)
    }

    companion object {
        const val aPosition: Int = 0
        const val aTexCoords: Int = 1
        const val uAspectRatio: Int = 0
        const val uInvert: Int = 1
        const val uImage: Int = 2
        const val uFactor: Int = 3
    }
}