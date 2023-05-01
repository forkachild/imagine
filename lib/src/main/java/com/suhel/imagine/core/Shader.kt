package com.suhel.imagine.core

class Shader(val program: Int) {

    companion object {
        const val aPosition: Int = 0
        const val aTexCoords: Int = 1
        const val uAspectRatio: Int = 0
        const val uInvert: Int = 1
        const val uImage: Int = 2
        const val uIntensity: Int = 3
    }
}