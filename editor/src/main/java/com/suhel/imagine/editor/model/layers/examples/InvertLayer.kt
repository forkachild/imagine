package com.suhel.imagine.editor.model.layers.examples

import com.suhel.imagine.editor.model.layers.EffectLayer

class InvertLayer : EffectLayer(
    "Invert",
    """
        vec4 process(vec4 color) {
            return vec4(1.0 - color.r, 1.0 - color.g, 1.0 - color.b, color.a);
        }
    """.trimIndent()
)