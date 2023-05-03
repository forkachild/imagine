package com.suhel.imagine.editor.layers

import com.suhel.imagine.types.Layer

abstract class EffectLayer(
    val name: String,
    final override val source: String
) : Layer {

    var factor: Float = 1.0f
    final override val intensity: Float
        get() = factor

}