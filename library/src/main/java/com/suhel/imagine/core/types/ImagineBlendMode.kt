package com.suhel.imagine.core.types

/**
 * Represents the algorithm which will be used to blend
 * the processed layer with the previous layer
 */
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
    Luminosity,
}