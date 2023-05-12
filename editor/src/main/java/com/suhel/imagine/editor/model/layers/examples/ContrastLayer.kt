package com.suhel.imagine.editor.model.layers.examples

import com.suhel.imagine.editor.model.layers.EffectLayer

class ContrastLayer : EffectLayer(
    "Contrast",
    """
        vec4 process(vec4 color) {
            return vec4(color.rgb * color.rgb, color.a);
        }
    """.trimIndent()
)