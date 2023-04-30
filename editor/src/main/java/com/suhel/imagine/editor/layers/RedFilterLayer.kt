package com.suhel.imagine.editor.layers

class RedFilterLayer : EditableLayer(
    """
        vec4 process(vec4 color) {
            return vec4(color.r, 0.0, 0.0, 1.0);
        }
    """.trimIndent()
)