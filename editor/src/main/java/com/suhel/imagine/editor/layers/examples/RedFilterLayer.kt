package com.suhel.imagine.editor.layers.examples

import com.suhel.imagine.editor.layers.EffectLayer

class RedFilterLayer : EffectLayer(
    "Red Filter",
    """
        vec4 process(vec4 color) {
            return vec4(color.r, 0.0, 0.0, 1.0);
        }
    """.trimIndent()
)