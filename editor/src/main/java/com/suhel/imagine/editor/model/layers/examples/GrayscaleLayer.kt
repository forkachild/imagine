package com.suhel.imagine.editor.model.layers.examples

import com.suhel.imagine.editor.model.layers.EffectLayer

class GrayscaleLayer : EffectLayer(
    "Grayscale",
    """
        vec3 process(vec3 color) {
            float avg = (0.2126 * color.r) + (0.7152 * color.g) + (0.0722 * color.b);
            return vec3(avg);
        }
    """.trimIndent()
)