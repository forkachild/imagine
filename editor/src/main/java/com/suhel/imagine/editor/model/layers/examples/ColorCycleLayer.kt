package com.suhel.imagine.editor.model.layers.examples

import com.suhel.imagine.editor.model.layers.EffectLayer

class ColorCycleLayer : EffectLayer(
    "Color Cycle",
    """
        vec4 process(vec4 color) {
            return vec4(color.gbr, color.a);
        }
    """.trimIndent()
)