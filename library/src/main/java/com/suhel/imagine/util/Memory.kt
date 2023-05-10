package com.suhel.imagine.util

private val intArray = IntArray(1)

/**
 * DSL to provide indirection to pass a uni-length array
 * to obtain a handle in OpenGL
 */
internal fun getProxyInt(block: (IntArray) -> Unit): Int {
    block(intArray)
    return intArray[0]
}

/**
 * DSL to provide indirection to pass a uni-length array
 * to set a handle in OpenGL
 */
internal fun setProxyInt(value: Int, block: (IntArray) -> Unit) {
    intArray[0] = value
    block(intArray)
}