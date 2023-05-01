package com.suhel.imagine.editor.layers.examples

import com.suhel.imagine.editor.layers.EffectLayer

class ColorCycleLayer : EffectLayer(
    "Color Cycle",
    """
        vec4 process(vec4 color) {
            return vec4(color.g, color.b, color.r, 1.0);
        }
    """.trimIndent()
)