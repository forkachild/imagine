package com.suhel.imagine.editor.model.layers.examples

import com.suhel.imagine.editor.model.layers.EffectLayer

class PassthroughLayer : EffectLayer(
    "Passthrough",
    """
        vec4 process(vec4 color) {
            return color;
        }
    """.trimIndent()
)