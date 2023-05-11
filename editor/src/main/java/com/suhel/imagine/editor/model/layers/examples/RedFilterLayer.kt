package com.suhel.imagine.editor.model.layers.examples

import com.suhel.imagine.editor.model.layers.EffectLayer

class RedFilterLayer : EffectLayer(
    "Red Filter",
    """
        vec3 process(vec3 color) {
            return vec3(color.r, 0.0, 0.0);
        }
    """.trimIndent()
)