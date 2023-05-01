package com.suhel.imagine.core

import com.suhel.imagine.types.Layer
import java.lang.ref.WeakReference

abstract class ImagineViewAdapter {
    private var weakImagineView: WeakReference<ImagineView> = WeakReference(null)

    abstract val layerCount: Int
    abstract fun getLayer(position: Int): Layer

    fun notifyLayersChanged() {
        weakImagineView.get()?.requestRender()
    }

    internal fun setImagineView(imagineView: ImagineView) {
        weakImagineView = WeakReference(imagineView)
    }
}