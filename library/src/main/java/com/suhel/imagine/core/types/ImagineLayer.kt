package com.suhel.imagine.core.types

import com.suhel.imagine.core.objects.ImagineShader

interface ImagineLayer {
    val source: String
    val intensity: Float

    fun create(program: ImagineShader.Program) {}

    fun bind(program: ImagineShader.Program) {}
}