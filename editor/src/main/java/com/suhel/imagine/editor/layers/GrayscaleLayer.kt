package com.suhel.imagine.editor.layers

class GrayscaleLayer : EditableLayer(
    """
        vec4 process(vec4 color) {
            float avg = (0.2126 * color.r) + (0.7152 * color.g) + (0.0722 * color.b);
            return vec4(avg, avg, avg, 1.0);
        }
    """.trimIndent()
)