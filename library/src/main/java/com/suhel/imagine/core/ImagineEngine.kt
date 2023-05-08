package com.suhel.imagine.core

import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView.Renderer
import android.os.Handler
import android.os.Looper
import com.google.android.material.R
import com.google.android.material.color.MaterialColors
import com.suhel.imagine.core.objects.ImagineFramebuffer
import com.suhel.imagine.core.objects.ImagineLayerShader
import com.suhel.imagine.core.objects.ImagineLayerShaderFactory
import com.suhel.imagine.core.objects.ImagineQuad
import com.suhel.imagine.core.objects.ImagineSwapchain
import com.suhel.imagine.core.objects.ImagineTexture
import com.suhel.imagine.core.objects.ImagineViewport
import com.suhel.imagine.core.types.ImagineDimensions
import com.suhel.imagine.core.types.ImagineImageProvider
import com.suhel.imagine.core.types.ImagineLayer
import com.suhel.imagine.core.types.ImagineMatrix
import com.suhel.imagine.util.weakRefOf
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ImagineEngine(imagineView: ImagineView) : Renderer {

    var layers: List<ImagineLayer>?
        get() = state.layers
        set(value) {
            state = state.copy(layers = value)
        }

    var imageProvider: ImagineImageProvider?
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
            state = state.copy(onBitmap = value)
        }

    private val imagineView by weakRefOf(imagineView)
    private var renderContext: RenderContext = RenderContext.Blank
    private var state: State = State()
        set(value) {
            field = value
            renderContext = value.renderContext
        }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        updateSurfaceColor()

        state = state.copy(
            isReady = true,
            shaderFactory = ImagineLayerShaderFactory.create(),
            quad = ImagineQuad.create(),
        )
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        val oldDimensions = state.viewport?.dimensions
        val newDimensions = ImagineDimensions(width, height)

        if (oldDimensions != newDimensions)
            updateViewport(newDimensions)
    }

    override fun onDrawFrame(gl: GL10?) {
        runPendingOperations()
        draw()
    }

    fun exportBitmap() {
        state = state.copy(isPendingExport = true)
        imagineView?.requestRender()
    }

    fun updatePreview() {
        imagineView?.requestRender()
    }

    private fun updateSurfaceColor() {
        val color = imagineView?.let {
            MaterialColors.getColor(it, R.attr.colorSurface, Color.BLACK)
        } ?: Color.BLACK

        val red = (color shr 16) and 0xFF
        val green = (color shr 8) and 0xFF
        val blue = color and 0xFF

        GLES20.glClearColor(
            red.toFloat() / 255,
            green.toFloat() / 255,
            blue.toFloat() / 255,
            1.0f
        )
    }

    private fun runPendingOperations() {
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
                    ImagineTexture.create(bitmap, mipmap = true, recycleBitmap = true)
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
                    ImagineSwapchain.create(
                        texture.dimensions.fitInside(viewport.dimensions)
                    )
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
                    ImagineMatrix.ofAspectFit(
                        image.dimensions.aspectRatio,
                        viewport.dimensions.aspectRatio
                    )
                else null,
            )
        }
    }

    private fun updateViewport(dimensions: ImagineDimensions) {
        state = state.copy(
            viewport = ImagineViewport(dimensions),
            isPendingSwapchainUpdate = true,
            isPendingAspectRatioMatrixUpdate = true,
        )
    }

    private fun draw() {
        renderContext.draw()

        if (state.isPendingExport) {
            state = state.copy(isPendingExport = false)
        }
    }

    internal data class State(
        val mainThreadHandler: Handler = Handler(Looper.getMainLooper()),
        val isReady: Boolean = false,
        val isPendingExport: Boolean = false,
        val isPendingTextureUpdate: Boolean = false,
        val isPendingSwapchainUpdate: Boolean = false,
        val isPendingAspectRatioMatrixUpdate: Boolean = false,
        val quad: ImagineQuad? = null,
        val shaderFactory: ImagineLayerShaderFactory? = null,
        val layers: List<ImagineLayer>? = null,
        val viewport: ImagineViewport? = null,
        val onBitmap: ((Bitmap) -> Unit)? = null,
        val imageProvider: ImagineImageProvider? = null,
        val image: ImagineTexture? = null,
        val swapchain: ImagineSwapchain? = null,
        val aspectRatioMatrix: ImagineMatrix? = null,
    ) {

        val renderContext: RenderContext
            get() {
                if (!isReady
                    || isPendingTextureUpdate
                    || isPendingSwapchainUpdate
                    || isPendingAspectRatioMatrixUpdate
                    || quad == null
                    || viewport == null
                    || swapchain == null
                    || shaderFactory == null
                    || image == null
                    || aspectRatioMatrix == null
                ) return RenderContext.Blank

                if (isPendingExport && layers != null && onBitmap != null)
                    return RenderContext.Output(
                        quad,
                        image,
                        shaderFactory,
                        layers,
                        mainThreadHandler,
                        onBitmap
                    )

                return RenderContext.Preview(
                    quad,
                    image,
                    shaderFactory,
                    viewport,
                    swapchain,
                    layers,
                    aspectRatioMatrix,
                )
            }

    }

    internal sealed class RenderContext {

        abstract fun draw()

        protected fun drawActual(
            quad: ImagineQuad,
            shader: ImagineLayerShader,
            framebuffer: ImagineFramebuffer,
            dimensions: ImagineDimensions,
            texture: ImagineTexture,
            aspectRatioMatrix: ImagineMatrix,
            invertMatrix: ImagineMatrix,
            intensity: Float = 1.0f,
        ) {
            shader.use()
            shader.bindAspectRatioMatrix(aspectRatioMatrix)
            shader.bindInvertMatrix(invertMatrix)
            shader.bindImage(texture)
            shader.bindIntensity(intensity)

            framebuffer.bind()
            GLES20.glViewport(0, 0, dimensions.width, dimensions.height)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            quad.draw()
        }

        internal object Blank : RenderContext() {

            override fun draw() {
                ImagineFramebuffer.default.bind()
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            }

        }

        internal class Preview(
            private val quad: ImagineQuad,
            private val image: ImagineTexture,
            private val shaderFactory: ImagineLayerShaderFactory,
            private val viewport: ImagineViewport,
            private val swapchain: ImagineSwapchain,
            private val layers: List<ImagineLayer>?,
            private val aspectRatioMatrix: ImagineMatrix,
        ) : RenderContext() {

            override fun draw() {
                if (layers.isNullOrEmpty()) {
                    drawActual(
                        quad,
                        shaderFactory.bypassShader,
                        ImagineFramebuffer.default,
                        viewport.dimensions,
                        image,
                        aspectRatioMatrix,
                        ImagineMatrix.invertY,
                    )
                } else {
                    layers.forEachIndexed { index, layer ->
                        val isSourceImage = index == 0
                        val isFrontBuffer = index == layers.lastIndex
                        val isInverted = index == 0
                        val shader = shaderFactory.getLayerShader(layer) ?: return@forEachIndexed

                        layer.bind(shader.program)

                        drawActual(
                            quad,
                            shader,
                            if (isFrontBuffer) ImagineFramebuffer.default else swapchain.framebuffer,
                            if (isFrontBuffer) viewport.dimensions else swapchain.dimensions,
                            if (isSourceImage) image else swapchain.texture,
                            if (isFrontBuffer) aspectRatioMatrix else ImagineMatrix.identity,
                            if (isInverted) ImagineMatrix.invertY else ImagineMatrix.identity,
                            layer.intensity,
                        )

                        swapchain.swap()
                    }
                }
            }

        }

        internal class Output(
            private val quad: ImagineQuad,
            private val image: ImagineTexture,
            private val shaderFactory: ImagineLayerShaderFactory,
            private val layers: List<ImagineLayer>,
            private val mainThreadHandler: Handler,
            private val onBitmap: (Bitmap) -> Unit,
        ) : RenderContext() {

            override fun draw() {
                val swapchain = ImagineSwapchain.create(image.dimensions)
                GLES20.glViewport(0, 0, image.dimensions.width, image.dimensions.height)

                layers.forEachIndexed { index, layer ->
                    val isSourceImage = index == 0
                    val shader = shaderFactory.getLayerShader(layer) ?: return@forEachIndexed

                    layer.bind(shader.program)

                    drawActual(
                        quad,
                        shader,
                        swapchain.framebuffer,
                        image.dimensions,
                        if (isSourceImage) image else swapchain.texture,
                        ImagineMatrix.identity,
                        ImagineMatrix.identity,
                        layer.intensity,
                    )

                    swapchain.swap()
                }

                val bitmap = swapchain.bitmap
                swapchain.release()

                mainThreadHandler.post {
                    onBitmap(bitmap)
                }
            }

        }

    }

}