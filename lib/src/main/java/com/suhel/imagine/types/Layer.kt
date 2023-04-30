package com.suhel.imagine.types

interface Layer {
    val source: String
    val blend: Float

    fun bind() {}
}