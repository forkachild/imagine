package com.suhel.imagine.types

interface Layer {
    val source: String
    val intensity: Float

    fun create(program: Int) {}
    fun bind(program: Int) {}
}