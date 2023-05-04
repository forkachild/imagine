package com.suhel.imagine.core

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLSurfaceView.Renderer
import android.os.Handler
import android.os.Looper
import com.suhel.imagine.types.Dimension
import com.suhel.imagine.types.ImageProvider
import com.suhel.imagine.types.Layer
import com.suhel.imagine.types.Mat4
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ImagineEngine(imagineView: ImagineView) : Renderer {

    var layers: List<Layer>?
        get() = state.layers
        set(value) {
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

    private val weakImagineView: WeakReference<ImagineView> = WeakReference(imagineView)
    private val imagineView: ImagineView?
        get() = weakImagineView.get()

    private val mainThread = Handler(Looper.getMainLooper())
    private var state: State = State()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        state = state.copy(
            isReady = true,
            shaderFactory = LayerShaderFactory.create(),
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
        updateResources()
        renderLayers()
    }

    fun updatePreview() {
        imagineView?.requestRender()
    }

    fun exportBitmap() {
        state = state.copy(
            isPendingExport = true,
        )
        imagineView?.requestRender()
    }

    private fun updateResources() {
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
        if (!state.isReady) return

        val shaderFactory = state.shaderFactory ?: return
        val quad = state.quad ?: return
        val aspectRatioMatrix = state.aspectRatioMatrix ?: return
        val image = state.image ?: return
        val viewport = state.viewport ?: return
        val layers = state.layers

        if (layers != null && layers.isNotEmpty()) {
            val onBitmap = state.onBitmap
            val isPendingExport = state.isPendingExport && onBitmap != null
            val swapchain = if (isPendingExport)
                Swapchain.create(image.dimension) else (state.swapchain ?: return)

            layers.forEachIndexed { index, layer ->
                val isFirstIndex = index == 0
                val isLastIndex = index == layers.lastIndex

                val isFrontBuffer = isLastIndex && !isPendingExport
                val isInverted = isFirstIndex && !isPendingExport

                val texture = if (isFirstIndex) image else swapchain.texture
                val framebuffer =
                    if (isFrontBuffer) Framebuffer.default else swapchain.framebuffer
                val dimension =
                    if (isFrontBuffer) viewport.dimension else swapchain.dimension

                dimension.setAsViewport()
                framebuffer.bind()
                framebuffer.clear()

                renderLayer(
                    layer,
                    quad,
                    texture,
                    shaderFactory,
                    swapchain,
                    aspectRatioMatrix,
                    isFrontBuffer,
                    isInverted,
                )

                swapchain.next()
            }

            if (isPendingExport) {
                val bitmap = swapchain.bitmap
                swapchain.release()

                mainThread.post {
                    onBitmap!!(bitmap)
                }

                state = state.copy(
                    isPendingExport = false,
                )
            }
        } else {
            Framebuffer.default.apply {
                bind()
                clear()
            }

            renderBypass(
                quad,
                image,
                shaderFactory,
                aspectRatioMatrix,
            )
        }
    }

    private fun renderLayer(
        layer: Layer,
        quad: Quad,
        texture: Texture,
        shaderFactory: LayerShaderFactory,
        swapchain: Swapchain,
        aspectRatioMatrix: Mat4,
        isFrontBuffer: Boolean,
        isInverted: Boolean,
    ) {
        val shader = shaderFactory.getLayerShader(layer) ?: return

        shader.use()
        shader.setAspectRatioMatrix(if (isFrontBuffer) aspectRatioMatrix else UnitMatrix)
        shader.setInvertMatrix(if (isInverted) InvertMatrix else UnitMatrix)
        shader.setImage(texture)
        shader.setIntensity(layer.intensity.coerceIn(0.0f, 1.0f))

        // Bind layer variables
        layer.bind(shader.program)

        // Finally draw!
        quad.draw()

        // Switch to next image in swapchain
        swapchain.next()
    }

    private fun renderBypass(
        quad: Quad,
        texture: Texture,
        shaderFactory: LayerShaderFactory,
        aspectRatioMatrix: Mat4,
    ) {
        val shader = shaderFactory.bypassShader

        shader.use()
        shader.setAspectRatioMatrix(aspectRatioMatrix)
        shader.setInvertMatrix(InvertMatrix)
        shader.setImage(texture)

        // Finally draw!
        quad.draw()
    }

    private data class State(
        val isReady: Boolean = false,
        val isPendingExport: Boolean = false,
        val isPendingTextureUpdate: Boolean = false,
        val isPendingSwapchainUpdate: Boolean = false,
        val isPendingAspectRatioMatrixUpdate: Boolean = false,
        val shaderFactory: LayerShaderFactory? = null,
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
        private val UnitMatrix: Mat4 = Mat4.ofUnit()
        private val InvertMatrix: Mat4 = Mat4.ofScale(1.0f, -1.0f, 1.0f)
    }

}