package com.suhel.imagine.editor.model.layers

class EffectLayerFactory(
    val name: String,
    private val factory: () -> EffectLayer
) {

    operator fun invoke(): EffectLayer = factory()

}