package com.suhel.imagine.core

import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import com.google.android.material.R
import com.google.android.material.color.MaterialColors
import com.suhel.imagine.BuildConfig
import com.suhel.imagine.core.ImagineEngine.RenderContext.Blank
import com.suhel.imagine.core.ImagineEngine.RenderContext.Export
import com.suhel.imagine.core.ImagineEngine.RenderContext.Preview
import com.suhel.imagine.core.objects.ImagineFramebuffer
import com.suhel.imagine.core.objects.ImagineLayerShader
import com.suhel.imagine.core.objects.ImagineLayerShaderFactory
import com.suhel.imagine.core.objects.ImagineQuad
import com.suhel.imagine.core.objects.ImagineTexture
import com.suhel.imagine.core.objects.ImagineTosschain
import com.suhel.imagine.core.objects.ImagineViewport
import com.suhel.imagine.core.types.ImagineBlendMode
import com.suhel.imagine.core.types.ImagineDimensions
import com.suhel.imagine.core.types.ImagineImageProvider
import com.suhel.imagine.core.types.ImagineLayer
import com.suhel.imagine.core.types.ImagineMatrix
import com.suhel.imagine.util.weakRefTo
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * The workhorse responsible for application of layers on a source bitmap
 * and either showing it on the viewport in "preview" mode or generating
 * a [Bitmap] on the UI thread in "export" mode.
 *
 * It is powered OpenGL ES 2.0, thus uses the GPU to apply the layer effects
 *
 * Setup is done in 2 simple lines
 * ```
 * val engine = ImagineEngine(imagineView)
 * imagineView.engine = engine
 * ```
 *
 * @param imagineView The [ImagineView] to attach this to
 */
class ImagineEngine(imagineView: ImagineView) {

    /**
     * Property to assign/query the layers applied on the image
     */
    var layers: List<ImagineLayer>?
        get() = state.layers
        set(value) {
            state = state.copy(layers = value)
        }

    /**
     * Property to set/get the image provider
     */
    var imageProvider: ImagineImageProvider?
        get() = state.imageProvider
        set(value) {
            state = state.copy(
                imageProvider = value,
                isPendingTextureUpdate = true,
                isPendingTosschainUpdate = true,
                isPendingAspectRatioMatrixUpdate = true,
            )
        }

    /**
     * Property to set the lambda which will be called with the
     * generated [Bitmap] in "export" mode
     */
    var onBitmap: ((Bitmap) -> Unit)?
        get() = state.onBitmap
        set(value) {
            state = state.copy(onBitmap = value)
        }

    /**
     * Holds a weak reference to the [ImagineView] used to
     * dispatch commands
     */
    private val imagineView by weakRefTo(imagineView)

    /**
     * Holds the current [RenderContext] to use for drawing
     */
    private var renderContext: RenderContext = Blank

    /**
     * Holds the current [State] of this engine which are updates
     * due to certain events.
     *
     * Since the [renderContext] derives from this, it gets updated
     * whenever this is updated.
     */
    private var state: State = State()
        set(value) {
            field = value
            renderContext = value.renderContext
        }

    internal inner class Renderer : GLSurfaceView.Renderer {

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            // Set the canvas clear color beforehand
            updateSurfaceColor()

            // Initialize the engine state to ready, with the essential
            // resources pre-allocated for later use
            state = state.copy(
                isReady = true,
                shaderFactory = imagineView?.context?.let { ImagineLayerShaderFactory.create(it) },
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
            // There are some operations queued to run on the
            // OpenGL context. Do these first
            runPendingOperations()

            // Finally draw the layers
            draw()
        }
    }

    init {
        // Set the renderer and render mode whenever this is instantiated
        imagineView.setRenderer(Renderer())
        imagineView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        if (BuildConfig.DEBUG)
            imagineView.debugFlags =
                GLSurfaceView.DEBUG_LOG_GL_CALLS or GLSurfaceView.DEBUG_CHECK_GL_ERROR
    }

    /**
     * Invoke an export operation on the engine. It will
     * generate a bitmap and call [onBitmap] lambda on the UI thread
     */
    fun exportBitmap() {
        if (!state.layers.isNullOrEmpty() && state.onBitmap != null) {
            state = state.copy(isPendingExport = true)
            imagineView?.requestRender()
        }
    }

    /**
     * Updates the viewport with the layers applied on the
     * source image
     */
    fun updatePreview() {
        imagineView?.requestRender()
    }

    /**
     * Update the clear color of OpenGL context
     */
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

    /**
     * Run queued operations
     */
    private fun runPendingOperations() {
        updateTexture()
        updateTosschain()
        updateAspectRatioMatrix()
    }

    /**
     * Update the source texture
     */
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

    /**
     * Update the tosschain to accommodate for a new image and/or viewport
     */
    private fun updateTosschain() {
        if (state.isPendingTosschainUpdate) {
            state.tosschain?.release()

            val texture = state.image
            val viewport = state.viewport

            state = state.copy(
                isPendingTosschainUpdate = false,
                tosschain = if (texture != null && viewport != null)
                    ImagineTosschain.create(
                        texture.dimensions.fitInside(viewport.dimensions)
                    )
                else null,
            )
        }
    }

    /**
     * Updates the aspect ratio matrix whenever the image and/or viewport
     * changes
     */
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

    /**
     * Updates the dimensions of the viewport
     *
     * @param dimensions The [ImagineDimensions] of the viewport
     */
    private fun updateViewport(dimensions: ImagineDimensions) {
        state = state.copy(
            viewport = ImagineViewport(dimensions),
            isPendingTosschainUpdate = true,
            isPendingAspectRatioMatrixUpdate = true,
        )
    }

    /**
     * Draw the layers
     */
    private fun draw() {
        renderContext.draw()

        // If an export was pending, reset it
        if (state.isPendingExport) {
            state = state.copy(isPendingExport = false)
        }
    }

    /**
     * Represents the current state of the engines. It holds all the
     * information required for the engine in either "preview" or "export".
     * It also helps in deriving a [RenderContext] from the internal state
     *
     * @property mainThreadHandler Used to post [onBitmap] calls on the UI thread
     * @property isReady Whether the engine is ready yet
     * @property isPendingExport Whether an export operation is pending
     * @property isPendingTextureUpdate Whether the [imageProvider] has been updated
     * invoking a re-load of the source image texture
     * @property isPendingTosschainUpdate Whether the [tosschain] needs to be updated
     * due to a change in either [image] or [viewport]
     * @property isPendingAspectRatioMatrixUpdate Whether the [aspectRatioMatrix] needs
     * to be updated due to a change in either [image] or [viewport]
     * @property quad A [ImagineQuad] object used to render the quad geometry representing
     * the image canvas
     * @property shaderFactory A [ImagineLayerShaderFactory] tasked to cache and supply
     * linked shaders for corresponding layers and also a copy shader
     * @property layers A list of [ImagineLayer]s which are applied on the image one after another
     * @property viewport A [ImagineViewport] to hold the current dimensions of the viewport
     * @property onBitmap A lambda that is called with a valid [Bitmap] in "export" mode
     * @property imageProvider An implementation of [ImagineImageProvider] to provide a [Bitmap]
     * @property image A [ImagineTexture] used to hold the mipmapped source image texture
     * @property tosschain A [ImagineTosschain] used to apply an arbitrary number of
     * layers one after another
     * @property aspectRatioMatrix A [ImagineMatrix] used to maintain the aspect ratio of the
     * image in the viewport
     */
    internal data class State(
        val mainThreadHandler: Handler = Handler(Looper.getMainLooper()),
        val isReady: Boolean = false,
        val isPendingExport: Boolean = false,
        val isPendingTextureUpdate: Boolean = false,
        val isPendingTosschainUpdate: Boolean = false,
        val isPendingAspectRatioMatrixUpdate: Boolean = false,
        val quad: ImagineQuad? = null,
        val shaderFactory: ImagineLayerShaderFactory? = null,
        val layers: List<ImagineLayer>? = null,
        val viewport: ImagineViewport? = null,
        val onBitmap: ((Bitmap) -> Unit)? = null,
        val imageProvider: ImagineImageProvider? = null,
        val image: ImagineTexture? = null,
        val tosschain: ImagineTosschain? = null,
        val aspectRatioMatrix: ImagineMatrix? = null,
    ) {

        /**
         * Obtain an instance of [RenderContext] based on the current
         * engine state
         */
        val renderContext: RenderContext
            get() {
                // Unless the required conditions are met, just display a blank screen
                if (!isReady
                    || isPendingTextureUpdate
                    || isPendingTosschainUpdate
                    || isPendingAspectRatioMatrixUpdate
                    || quad == null
                    || viewport == null
                    || tosschain == null
                    || shaderFactory == null
                    || image == null
                    || aspectRatioMatrix == null
                ) return RenderContext.Blank

                // Export mode
                if (isPendingExport && !layers.isNullOrEmpty() && onBitmap != null)
                    return RenderContext.Export(
                        quad,
                        image,
                        shaderFactory,
                        layers,
                        mainThreadHandler,
                        onBitmap
                    )

                // Normal preview mode
                return RenderContext.Preview(
                    quad,
                    image,
                    shaderFactory,
                    viewport,
                    tosschain,
                    layers,
                    aspectRatioMatrix,
                )
            }

    }

    /**
     * Abstraction with 3 variants to render the layers
     * - [Blank]
     * - [Preview]
     * - [Export]
     */
    internal sealed class RenderContext {

        /**
         * Draw the layers
         */
        abstract fun draw()

        /**
         * Draw one layer of effect
         *
         * @param quad The quad to draw
         * @param shader The shader to use and bind values to
         * @param framebuffer The target framebuffer to draw into
         * @param dimensions The dimensions of the draw
         * @param texture The image to sample from
         * @param aspectRatioMatrix The aspect ratio matrix to scale
         * @param invertMatrix The upside-down flip matrix
         * @param intensity The intensity of application of this layer
         */
        protected fun drawActual(
            quad: ImagineQuad,
            shader: ImagineLayerShader,
            framebuffer: ImagineFramebuffer,
            dimensions: ImagineDimensions,
            texture: ImagineTexture,
            aspectRatioMatrix: ImagineMatrix,
            invertMatrix: ImagineMatrix,
            intensity: Float = 1.0f,
            blendMode: ImagineBlendMode,
        ) {
            shader.bind()
            shader.bindAspectRatioMatrix(aspectRatioMatrix)
            shader.bindInvertMatrix(invertMatrix)
            shader.bindImage(texture)
            shader.bindIntensity(intensity)
            shader.bindBlendMode(blendMode)

            framebuffer.bind()
            GLES20.glViewport(0, 0, dimensions.width, dimensions.height)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            quad.draw()
        }

        /**
         * Just clear the screen and do nothing else
         */
        internal object Blank : RenderContext() {

            override fun draw() {
                ImagineFramebuffer.default.bind()
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            }

        }

        /**
         * Update the viewport with the final image after layers have been applied.
         * If no layers then just copy the source image into the viewport
         *
         * @property quad A [ImagineQuad] object used to render the quad geometry representing
         * the image canvas
         * @property shaderFactory A [ImagineLayerShaderFactory] tasked to cache and supply
         * linked shaders for corresponding layers and also a copy shader
         * @property layers A list of [ImagineLayer]s which are applied on the image one after another
         * @property viewport A [ImagineViewport] to hold the current dimensions of the viewport
         * @property onBitmap A lambda that is called with a valid [Bitmap] in "export" mode
         * @property imageProvider An implementation of [ImagineImageProvider] to provide a [Bitmap]
         * @property image A [ImagineTexture] used to hold the mipmapped source image texture
         * @property tosschain A [ImagineTosschain] used to apply an arbitrary number of
         * layers one after another
         * @property aspectRatioMatrix A [ImagineMatrix] used to maintain the aspect ratio of the
         * image in the viewport
         */
        internal class Preview(
            private val quad: ImagineQuad,
            private val image: ImagineTexture,
            private val shaderFactory: ImagineLayerShaderFactory,
            private val viewport: ImagineViewport,
            private val tosschain: ImagineTosschain,
            private val layers: List<ImagineLayer>?,
            private val aspectRatioMatrix: ImagineMatrix,
        ) : RenderContext() {

            override fun draw() {
                // Conditionally either apply layers or just copy from source
                if (layers.isNullOrEmpty()) {
                    // Render a copy of the source
                    drawActual(
                        quad,
                        shaderFactory.bypassShader,
                        ImagineFramebuffer.default,
                        viewport.dimensions,
                        image,
                        aspectRatioMatrix,
                        ImagineMatrix.invertY,
                        intensity = 1.0f,
                        ImagineBlendMode.Normal,
                    )
                } else {
                    // Apply each layer iteratively
                    layers.forEachIndexed { index, layer ->
                        // Whether we should consider the source texture or tosschain texture
                        val isSourceImage = index == 0

                        // Whether we should be rendering to the viewport framebuffer
                        // or in tosschain framebuffer
                        val isFrontBuffer = index == layers.lastIndex

                        // Should the image be inverted while sampling
                        val isInverted = index == 0

                        // Obtain the shader, otherwise stop applying layers
                        val shader = shaderFactory.getLayerShader(layer) ?: return@forEachIndexed

                        // Allow the layer implementation to update any
                        // custom uniform(s)
                        layer.bind(shader.program)

                        // Render one layer
                        drawActual(
                            quad,
                            shader,
                            if (isFrontBuffer) ImagineFramebuffer.default else tosschain.framebuffer,
                            if (isFrontBuffer) viewport.dimensions else tosschain.dimensions,
                            if (isSourceImage) image else tosschain.texture,
                            if (isFrontBuffer) aspectRatioMatrix else ImagineMatrix.identity,
                            if (isInverted) ImagineMatrix.invertY else ImagineMatrix.identity,
                            layer.intensity,
                            layer.blendMode,
                        )

                        // Swap framebuffers
                        tosschain.swap()
                    }
                }
            }

        }

        /**
         * Render the layers into a back-buffer and call a lambda with the
         * [Bitmap] extracted from it
         *
         * @property mainThreadHandler Used to post [onBitmap] calls on the UI thread
         * @property quad A [ImagineQuad] object used to render the quad geometry representing
         * the image canvas
         * @property shaderFactory A [ImagineLayerShaderFactory] tasked to cache and supply
         * linked shaders for corresponding layers and also a copy shader
         * @property layers A list of [ImagineLayer]s which are applied on the image one after another
         * @property onBitmap A lambda that is called with a valid [Bitmap] in "export" mode
         * @property imageProvider An implementation of [ImagineImageProvider] to provide a [Bitmap]
         * @property image A [ImagineTexture] used to hold the mipmapped source image texture
         */
        internal class Export(
            private val quad: ImagineQuad,
            private val image: ImagineTexture,
            private val shaderFactory: ImagineLayerShaderFactory,
            private val layers: List<ImagineLayer>,
            private val mainThreadHandler: Handler,
            private val onBitmap: (Bitmap) -> Unit,
        ) : RenderContext() {

            override fun draw() {
                // Create a temporary full-sized tosschain for this task
                val tosschain = ImagineTosschain.create(image.dimensions)

                layers.forEachIndexed { index, layer ->
                    // Obtain the shader, otherwise stop applying layers
                    val shader = shaderFactory.getLayerShader(layer) ?: return@forEachIndexed

                    // Whether we should consider the source texture or tosschain texture
                    val isSourceImage = index == 0

                    // Allow the layer implementation to update any
                    // custom uniform(s)
                    layer.bind(shader.program)

                    // Render one layer
                    drawActual(
                        quad,
                        shader,
                        tosschain.framebuffer,
                        image.dimensions,
                        if (isSourceImage) image else tosschain.texture,
                        ImagineMatrix.identity,
                        ImagineMatrix.identity,
                        layer.intensity,
                        layer.blendMode,
                    )

                    // Swap framebuffers
                    tosschain.swap()
                }

                // Obtain the bitmap
                val bitmap = tosschain.bitmap

                // Free the temporary high-res tosschain
                tosschain.release()

                // Post the bitmap on the lambda in the UI thread
                mainThreadHandler.post {
                    onBitmap(bitmap)
                }
            }

        }

    }

}