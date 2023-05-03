package com.suhel.imagine.core

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import com.suhel.imagine.types.Dimension
import com.suhel.imagine.types.ImageProvider
import com.suhel.imagine.types.Layer
import com.suhel.imagine.types.Mat4
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ImagineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs), Renderer {

    var layers: List<Layer>?
        get() = state.layers
        set(value) {
            if (state.layers != value)
                state = state.copy(
                    layers = value,
                )
        }

    var imageProvider: ImageProvider?
        get() = state.imageProvider
        set(value) {
            state = state.copy(
                imageProvider = value,
                isPendingTextureUpdate = true,
                isPendingSwapchainUpdate = true,
                isPendingAspectRatioMatrixUpdate = true,
            )
        }

    var onBitmap: ((Bitmap) -> Unit)?
        get() = state.onBitmap
        set(value) {
            state = state.copy(
                onBitmap = value,
            )
        }

    private val mainThread = Handler(Looper.getMainLooper())
    private var state: State = State()

    init {
        setEGLContextClientVersion(3)
        debugFlags = DEBUG_CHECK_GL_ERROR or DEBUG_LOG_GL_CALLS
        setRenderer(this)
        renderMode = RENDERMODE_WHEN_DIRTY
        preserveEGLContextOnPause = true
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        state = state.copy(
            isReady = true,
            shaderFactory = ShaderFactory.create(),
            quad = Quad.create(),
        )
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        val oldDimension = state.viewport?.dimension
        val newDimension = Dimension(width, height)

        if (oldDimension == newDimension) return

        state = state.copy(
            viewport = Viewport(newDimension),
            isPendingSwapchainUpdate = true,
            isPendingAspectRatioMatrixUpdate = true,
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        update()
        renderLayers()
    }

    fun preview() {
        requestRender()
    }

    fun export() {
        state = state.copy(
            isPendingExport = true,
        )
        requestRender()
    }

    private fun update() {
        updateTexture()
        updateSwapchain()
        updateAspectRatioMatrix()
    }

    private fun updateTexture() {
        if (state.isPendingTextureUpdate) {
            state.image?.release()

            state = state.copy(
                isPendingTextureUpdate = false,
                image = state.imageProvider?.bitmap?.let { bitmap ->
                    Texture.create(bitmap, mipmap = true, recycleBitmap = true)
                },
            )
        }
    }

    private fun updateSwapchain() {
        if (state.isPendingSwapchainUpdate) {
            state.swapchain?.release()

            val texture = state.image
            val viewport = state.viewport

            state = state.copy(
                isPendingSwapchainUpdate = false,
                swapchain = if (texture != null && viewport != null)
                    Swapchain.create(texture.dimension.fitInside(viewport.dimension))
                else null,
            )
        }
    }

    private fun updateAspectRatioMatrix() {
        if (state.isPendingAspectRatioMatrixUpdate) {
            val image = state.image
            val viewport = state.viewport

            state = state.copy(
                isPendingAspectRatioMatrixUpdate = false,
                aspectRatioMatrix = if (image != null && viewport != null)
                    Mat4.ofAspectFit(image.dimension.aspectRatio, viewport.dimension.aspectRatio)
                else null,
            )
        }
    }

    private fun renderLayers() {
        val onBitmap = state.onBitmap
        val isPendingExport = state.isPendingExport && onBitmap != null
        val shaderFactory = state.shaderFactory ?: return
        val quad = state.quad ?: return
        val aspectRatioMatrix = state.aspectRatioMatrix ?: return
        val image = state.image ?: return
        val viewport = state.viewport ?: return
        val layers = state.layers ?: return
        val swapchain = if (isPendingExport)
            Swapchain.create(image.dimension) else (state.swapchain ?: return)

        layers.forEachIndexed { index, layer ->
            val isFirstIndex = index == 0
            val isLastIndex = index == layers.lastIndex

            val isFrontBuffer = isLastIndex && !isPendingExport
            val isInverted = isFirstIndex && !isPendingExport

            val texture = if (isFirstIndex) image else swapchain.texture()
            val framebuffer = if (isFrontBuffer) Framebuffer.Viewport else swapchain.framebuffer()
            val dimension = if (isFrontBuffer) viewport.dimension else swapchain.dimension

            dimension.setAsViewport()
            framebuffer.bind()
            framebuffer.clear()

            renderLayer(
                layer,
                quad,
                shaderFactory,
                texture,
                aspectRatioMatrix,
                isFrontBuffer,
                isInverted,
            )

            swapchain.next()
        }

        if (isPendingExport) {
            val dimension = image.dimension

            val buffer = ByteBuffer.allocateDirect(dimension.width * dimension.height * 4)
            GLES30.glReadPixels(
                0,
                0,
                dimension.width,
                dimension.height,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                buffer
            )
            swapchain.release()

            val bitmap = Bitmap.createBitmap(
                dimension.width,
                dimension.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)

            mainThread.post {
                onBitmap!!(bitmap)
            }

            state = state.copy(
                isPendingExport = false,
            )
        }
    }

    private fun renderLayer(
        layer: Layer,
        quad: Quad,
        shaderFactory: ShaderFactory,
        texture: Texture,
        aspectRatioMatrix: Mat4,
        isFrontBuffer: Boolean,
        isInverted: Boolean,
    ) {
        val shader = shaderFactory.getShader(layer) ?: return

        shader.use()
        shader.setAspectRatioMatrix(if (isFrontBuffer) aspectRatioMatrix else UnitMatrix)
        shader.setInvertMatrix(if (isInverted) InvertMatrix else UnitMatrix)
        shader.setImage(texture)
        shader.setIntensity(layer.intensity.coerceIn(0.0f, 1.0f))

        // Bind layer variables
        layer.bind(shader.program)

        // Finally draw!
        quad.draw()
    }

    private data class State(
        val isReady: Boolean = false,
        val isPendingExport: Boolean = false,
        val isPendingTextureUpdate: Boolean = false,
        val isPendingSwapchainUpdate: Boolean = false,
        val isPendingAspectRatioMatrixUpdate: Boolean = false,
        val shaderFactory: ShaderFactory? = null,
        val quad: Quad? = null,
        val viewport: Viewport? = null,
        val layers: List<Layer>? = null,
        val imageProvider: ImageProvider? = null,
        val image: Texture? = null,
        val swapchain: Swapchain? = null,
        val aspectRatioMatrix: Mat4? = null,
        val onBitmap: ((Bitmap) -> Unit)? = null,
    )

    companion object {
        private const val TAG = "ImagineView"
        private val UnitMatrix: Mat4 = Mat4.ofUnit()
        private val InvertMatrix: Mat4 = Mat4.ofScale(1.0f, -1.0f, 1.0f)
    }

}