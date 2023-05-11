package com.suhel.imagine.editor.model.layers

import com.suhel.imagine.core.types.ImagineBlendMode
import com.suhel.imagine.core.types.ImagineLayer
import com.suhel.imagine.editor.model.NamedBlendMode
import com.suhel.imagine.editor.model.sectionedBlendModes

abstract class EffectLayer(
    val name: String,
    final override val source: String
) : ImagineLayer {

    var layerIntensity: Float = 1.0f
    var layerVisible: Boolean = true
    var layerBlendMode: NamedBlendMode = sectionedBlendModes[0].items[0]

    override val blendMode: ImagineBlendMode
        get() = layerBlendMode.blendMode

    final override val intensity: Float
        get() = if (layerVisible) layerIntensity else 0.0f

}