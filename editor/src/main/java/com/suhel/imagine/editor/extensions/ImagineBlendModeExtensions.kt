package com.suhel.imagine.editor.extensions

import com.suhel.imagine.core.types.ImagineBlendMode

val ImagineBlendMode.displayText: String
    get() = when (this) {
        ImagineBlendMode.Normal -> "Normal"
        ImagineBlendMode.Darken -> "Darken"
        ImagineBlendMode.Multiply -> "Multiply"
        ImagineBlendMode.ColorBurn -> "Color burn"
        ImagineBlendMode.LinearBurn -> "Linear burn"
        ImagineBlendMode.DarkerColor -> "Darker color"
        ImagineBlendMode.Lighten -> "Lighten"
        ImagineBlendMode.Screen -> "Screen"
        ImagineBlendMode.ColorDodge -> "Color dodge"
        ImagineBlendMode.LinearDodge -> "Linear dodge"
        ImagineBlendMode.LighterColor -> "Lighter color"
        ImagineBlendMode.Overlay -> "Overlay"
        ImagineBlendMode.SoftLight -> "Soft light"
        ImagineBlendMode.HardLight -> "Hard light"
        ImagineBlendMode.VividLight -> "Vivid light"
        ImagineBlendMode.LinearLight -> "Linear light"
        ImagineBlendMode.PinLight -> "Pin light"
        ImagineBlendMode.HardMix -> "Hard mix"
        ImagineBlendMode.Difference -> "Difference"
        ImagineBlendMode.Exclusion -> "Exclusion"
        ImagineBlendMode.Subtract -> "Subtract"
        ImagineBlendMode.Divide -> "Divide"
        ImagineBlendMode.Hue -> "Hue"
        ImagineBlendMode.Saturation -> "Saturation"
        ImagineBlendMode.Color -> "Color"
        ImagineBlendMode.Luminosity -> "Luminosity"
    }