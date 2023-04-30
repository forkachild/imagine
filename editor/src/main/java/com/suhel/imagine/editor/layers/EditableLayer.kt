package com.suhel.imagine.editor.layers

import com.suhel.imagine.types.Layer

abstract class EditableLayer(final override val source: String): Layer {

    var factor: Float = 1.0f
    final override val blend: Float
        get() = factor

}