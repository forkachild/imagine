package com.suhel.imagine.editor.model.layers.examples

import com.suhel.imagine.editor.model.layers.EffectLayer

class ColorCycleLayer : EffectLayer(
    "Color Cycle",
    """
        vec3 process(vec3 color) {
            return color.gbr;
        }
    """.trimIndent()
)