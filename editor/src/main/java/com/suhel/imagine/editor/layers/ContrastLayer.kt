package com.suhel.imagine.editor.layers

class ContrastLayer : EditableLayer(
    """
        vec4 process(vec4 color) {
            return vec4(color.r * color.r, color.g * color.g, color.b * color.b, 1.0);
        }
    """.trimIndent()
)