package com.suhel.imagine.util

private val intArray = IntArray(1)

internal fun getProxyInt(block: (IntArray) -> Unit): Int {
    block(intArray)
    return intArray[0]
}

internal fun setProxyInt(value: Int, block: (IntArray) -> Unit) {
    intArray[0] = value
    block(intArray)
}