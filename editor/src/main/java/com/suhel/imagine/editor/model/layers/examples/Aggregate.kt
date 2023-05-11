package com.suhel.imagine.editor.model.layers.examples

import com.suhel.imagine.editor.model.layers.EffectLayerFactory

val allLayers = listOf(
    EffectLayerFactory("Passthrough") { PassthroughLayer() },
    EffectLayerFactory("Contrast") { ContrastLayer() },
    EffectLayerFactory("Grayscale") { GrayscaleLayer() },
    EffectLayerFactory("Invert") { InvertLayer() },
    EffectLayerFactory("Color Cycle") { ColorCycleLayer() },
    EffectLayerFactory("Black Keying") { BlackKeyingLayer() },
    EffectLayerFactory("Red Filter") { RedFilterLayer() },
)