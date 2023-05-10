package com.suhel.imagine.core

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

/**
 * A very thin extension on [GLSurfaceView] that sets the OpenGL version
 * and forces the context to be preserved on pause
 */
class ImagineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    init {
        // Set the OpenGL version
        setEGLContextClientVersion(2)
        preserveEGLContextOnPause = true
    }

}