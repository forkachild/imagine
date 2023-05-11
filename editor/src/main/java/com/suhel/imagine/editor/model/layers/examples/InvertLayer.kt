package com.suhel.imagine.editor.model.layers.examples

import com.suhel.imagine.editor.model.layers.EffectLayer

class InvertLayer : EffectLayer(
    "Invert",
    """
        vec3 process(vec3 color) {
            return vec3(1.0 - color.r, 1.0 - color.g, 1.0 - color.b);
        }
    """.trimIndent()
)