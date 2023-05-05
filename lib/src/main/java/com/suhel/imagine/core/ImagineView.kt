package com.suhel.imagine.core

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class ImagineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    init {
        setEGLContextClientVersion(2)
        debugFlags = DEBUG_CHECK_GL_ERROR or DEBUG_LOG_GL_CALLS
        preserveEGLContextOnPause = true
    }

    var engine: ImagineEngine? = null
        set(value) {
            field = value
            setRenderer(engine)
            renderMode = RENDERMODE_WHEN_DIRTY
        }

}