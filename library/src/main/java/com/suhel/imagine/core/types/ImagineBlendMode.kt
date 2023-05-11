package com.suhel.imagine.core.types

enum class ImagineBlendMode {
    // Normal
    Normal,

    // Darken
    Darken,
    Multiply,
    ColorBurn,
    LinearBurn,
    DarkerColor,

    // Lighten
    Lighten,
    Screen,
    ColorDodge,
    LinearDodge,
    LighterColor,

    // Contrast
    Overlay,
    SoftLight,
    HardLight,
    VividLight,
    LinearLight,
    PinLight,
    HardMix,

    // Inversion
    Difference,
    Exclusion,

    // Cancellation
    Subtract,
    Divide,

    // Component
    Hue,
    Saturation,
    Color,
    Luminosity;

    companion object {

        val all = values()

    }

}