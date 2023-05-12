package com.suhel.imagine.editor.model.layers.examples

import com.suhel.imagine.editor.model.layers.EffectLayer

class RedFilterLayer : EffectLayer(
    "Red Filter",
    """
        vec4 process(vec4 color) {
            return vec4(color.r, 0.0, 0.0, color.a);
        }
    """.trimIndent()
)