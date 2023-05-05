package com.suhel.imagine.editor.model.layers.examples

import com.suhel.imagine.editor.model.layers.EffectLayerFactory

val allLayers = listOf(
    EffectLayerFactory("Contrast") { ContrastLayer() },
    EffectLayerFactory("Grayscale") { GrayscaleLayer() },
    EffectLayerFactory("Invert") { InvertLayer() },
    EffectLayerFactory("Color cycle") { ColorCycleLayer() },
    EffectLayerFactory("Red filter") { RedFilterLayer() },
)