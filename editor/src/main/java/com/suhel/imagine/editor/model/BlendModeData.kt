package com.suhel.imagine.editor.model

import com.suhel.imagine.core.types.ImagineBlendMode
import com.suhel.imagine.editor.model.NamedBlendMode
import com.suhel.imagine.editor.ui.common.model.SectionedData

val sectionedBlendModes = listOf(
    SectionedData(
        "Normal",
        listOf(
            NamedBlendMode("Normal", ImagineBlendMode.Normal)
        ),
    ),
    SectionedData(
        "Darken",
        listOf(
            NamedBlendMode("Darken", ImagineBlendMode.Darken),
            NamedBlendMode("Multiply", ImagineBlendMode.Multiply),
            NamedBlendMode("Color Burn", ImagineBlendMode.ColorBurn),
            NamedBlendMode("Linear Burn", ImagineBlendMode.LinearBurn),
            NamedBlendMode("Darker Color", ImagineBlendMode.DarkerColor),
        ),
    ),
    SectionedData(
        "Lighten",
        listOf(
            NamedBlendMode("Lighten", ImagineBlendMode.Lighten),
            NamedBlendMode("Screen", ImagineBlendMode.Screen),
            NamedBlendMode("Color Dodge", ImagineBlendMode.ColorDodge),
            NamedBlendMode("Linear Dodge (Add)", ImagineBlendMode.LinearDodge),
            NamedBlendMode("Lighter Color", ImagineBlendMode.LighterColor),
        ),
    ),
    SectionedData(
        "Contrast",
        listOf(
            NamedBlendMode("Overlay", ImagineBlendMode.Overlay),
            NamedBlendMode("Soft Light", ImagineBlendMode.SoftLight),
            NamedBlendMode("Hard Light", ImagineBlendMode.HardLight),
            NamedBlendMode("Vivid Light", ImagineBlendMode.VividLight),
            NamedBlendMode("Linear Light", ImagineBlendMode.LinearLight),
            NamedBlendMode("Pin Light", ImagineBlendMode.PinLight),
            NamedBlendMode("Hard Mix", ImagineBlendMode.HardMix),
        ),
    ),
    SectionedData(
        "Inversion",
        listOf(
            NamedBlendMode("Difference", ImagineBlendMode.Difference),
            NamedBlendMode("Exclusion", ImagineBlendMode.Exclusion),
        ),
    ),
    SectionedData(
        "Cancellation",
        listOf(
            NamedBlendMode("Subtract", ImagineBlendMode.Subtract),
            NamedBlendMode("Divide", ImagineBlendMode.Divide),
        ),
    ),
    SectionedData(
        "Component",
        listOf(
            NamedBlendMode("Hue", ImagineBlendMode.Hue),
            NamedBlendMode("Saturation", ImagineBlendMode.Saturation),
            NamedBlendMode("Color", ImagineBlendMode.Color),
            NamedBlendMode("Luminosity", ImagineBlendMode.Luminosity),
        ),
    ),
)