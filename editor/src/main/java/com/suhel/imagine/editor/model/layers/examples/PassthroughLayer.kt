package com.suhel.imagine.editor.model.layers.examples

import com.suhel.imagine.editor.model.layers.EffectLayer

class PassthroughLayer : EffectLayer(
    "Passthrough",
    """
        vec3 process(vec3 color) {
            return color;
        }
    """.trimIndent()
)