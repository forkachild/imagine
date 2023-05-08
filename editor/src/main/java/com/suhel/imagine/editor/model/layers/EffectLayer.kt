package com.suhel.imagine.editor.model.layers

import com.suhel.imagine.core.types.ImagineLayer

abstract class EffectLayer(
    val name: String,
    final override val source: String
) : ImagineLayer {

    var factor: Float = 1.0f
    final override val intensity: Float
        get() = factor

}