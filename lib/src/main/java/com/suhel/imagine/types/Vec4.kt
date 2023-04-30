package com.suhel.imagine.types

class Vec4 {

    val array = FloatArray(4)

    fun setARGB(argb: Int) {
        array[0] = ((argb shr 16) and 0xFF).toFloat() / 0xFF
        array[1] = ((argb shr 8) and 0xFF).toFloat() / 0xFF
        array[2] = (argb and 0xFF).toFloat() / 0xFF
        array[3] = ((argb shr 24) and 0xFF).toFloat() / 0xFF
    }

}