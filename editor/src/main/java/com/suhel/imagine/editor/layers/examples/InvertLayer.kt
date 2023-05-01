package com.suhel.imagine.editor.layers.examples

import com.suhel.imagine.editor.layers.EffectLayer

class InvertLayer : EffectLayer(
    "Invert",
    """
        vec4 process(vec4 color) {
            return vec4(1.0 - color.r, 1.0 - color.g, 1.0 - color.b, 1.0);
        }
    """.trimIndent()
)