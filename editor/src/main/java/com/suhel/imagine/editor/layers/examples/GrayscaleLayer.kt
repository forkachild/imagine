package com.suhel.imagine.editor.layers.examples

import com.suhel.imagine.editor.layers.EffectLayer

class GrayscaleLayer : EffectLayer(
    "Grayscale",
    """
        vec4 process(vec4 color) {
            float avg = (0.2126 * color.r) + (0.7152 * color.g) + (0.0722 * color.b);
            return vec4(avg, avg, avg, 1.0);
        }
    """.trimIndent()
)