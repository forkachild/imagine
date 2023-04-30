package com.suhel.imagine.editor

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import android.util.AttributeSet
import com.suhel.imagine.core.Engine
import com.suhel.imagine.editor.layers.GrayscaleLayer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ImagineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs), Renderer {

    init {
        setEGLContextClientVersion(3)
        debugFlags = DEBUG_CHECK_GL_ERROR or DEBUG_LOG_GL_CALLS
        setRenderer(this)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    private val engine: Engine = Engine()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        engine.allocate()
        engine.layers = listOf(
            GrayscaleLayer(),
        )
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        engine.setViewportSize(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        engine.render()
    }

    fun loadBitmap(bitmap: Bitmap, shouldRecycle: Boolean = false) {
        queueEvent {
            engine.setBitmap(bitmap)
            if (shouldRecycle)
                bitmap.recycle()
        }
    }

}