package com.suhel.imagine.editor.layers

class InvertLayer : EditableLayer(
    """
        vec4 process(vec4 color) {
            return vec4(1.0 - color.r, 1.0 - color.g, 1.0 - color.b, 1.0);
        }
    """.trimIndent()
)