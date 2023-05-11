package com.suhel.imagine.editor.model.layers

import com.suhel.imagine.core.types.ImagineBlendMode
import com.suhel.imagine.core.types.ImagineLayer

abstract class EffectLayer(
    val name: String,
    final override val source: String
) : ImagineLayer {

    var layerIntensity: Float = 1.0f
    var layerVisible: Boolean = true
    var layerBlendModeIdx: Int = ImagineBlendMode.Normal.ordinal

    override val blendMode: ImagineBlendMode
        get() = ImagineBlendMode.all[layerBlendModeIdx]

    final override val intensity: Float
        get() = if (layerVisible) layerIntensity else 0.0f

}