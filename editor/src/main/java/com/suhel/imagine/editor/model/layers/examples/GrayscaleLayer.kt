package com.suhel.imagine.editor.model.layers.examples

import com.suhel.imagine.editor.model.layers.EffectLayer

class GrayscaleLayer : EffectLayer(
    "Grayscale",
    """
        vec4 process(vec4 color) {
            vec3 avg = vec3(dot(vec3(0.2126, 0.7152, 0.0722), color.rgb));
            return vec4(avg, color.a);
        }
    """.trimIndent()
)