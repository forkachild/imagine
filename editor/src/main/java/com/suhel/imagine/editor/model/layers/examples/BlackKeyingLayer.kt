package com.suhel.imagine.editor.model.layers.examples

import com.suhel.imagine.editor.model.layers.EffectLayer

class BlackKeyingLayer : EffectLayer(
    "Black keying",
    """
        vec4 process(vec4 color) {
            float alpha = color.rgb == vec3(0.0, 0.0, 0.0) ? 0.0 : color.a;
            return vec4(color.rgb, alpha);
        }
    """.trimIndent()
)