package com.suhel.imagine.core

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import android.util.AttributeSet
import com.suhel.imagine.types.BitmapProvider
import com.suhel.imagine.types.Dimension
import com.suhel.imagine.types.Layer
import com.suhel.imagine.types.Mat4
import com.suhel.imagine.util.ShaderFactory
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.reflect.KClass

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

    var layers: List<Layer>? = null
    var isPreview: Boolean = true

    private val layerShaderMap: MutableMap<KClass<out Layer>, Shader> = mutableMapOf()
    private val aspectRatioMatrix: Mat4 = Mat4()
    private val invertMatrix: Mat4 = Mat4.ofScale(1.0f, -1.0f, 1.0f)
    private val unitMatrix: Mat4 = Mat4()

    private var shaderFactory: ShaderFactory? = null
    private var quad: Quad? = null
    private var sourceTexture: Texture? = null
    private var previewSwapchain: Swapchain? = null
    private var exportSwapchain: Swapchain? = null
    private var viewportDimension: Dimension? = null
    private var bitmapDimension: Dimension? = null
    private var swapchainDimension: Dimension? = null

    private var isActive: Boolean = false
    private var pendingBitmapProvider: BitmapProvider? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        shaderFactory = ShaderFactory.obtain()
        quad = Quad.obtain()

        pendingBitmapProvider?.let { loadTexture(it) }
        isActive = true
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        setViewportSize(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        render()
    }

    fun reset() = queueEvent {
        shaderFactory?.release()
        shaderFactory = null

        quad?.release()
        quad = null

        sourceTexture?.release()
        sourceTexture = null

        previewSwapchain?.release()
        previewSwapchain = null

        viewportDimension = null
        bitmapDimension = null
        swapchainDimension = null
    }

    fun setBitmapProvider(bitmapProvider: BitmapProvider) = queueEvent {
        if (isActive)
            loadTexture(bitmapProvider)
        else
            pendingBitmapProvider = bitmapProvider
    }

    private fun setViewportSize(width: Int, height: Int) {
        viewportDimension = Dimension(width, height)
        updateDimensions()
    }

    private fun loadTexture(bitmapProvider: BitmapProvider) {
        val bitmap = bitmapProvider.bitmap
        sourceTexture?.release()
        sourceTexture = Texture.obtain(bitmap, true)
        bitmapDimension = Dimension.fromBitmap(bitmap)
        bitmap.recycle()
        updateDimensions()
    }

    private fun render() {
        val layers = this.layers ?: return
        val swapchain = this.previewSwapchain ?: return
        val sourceTexture = this.sourceTexture ?: return

        layers.forEachIndexed { index, layer ->
            val isFirstIndex = index == 0
            val isLastIndex = index == layers.lastIndex

            val texture = if (isFirstIndex)
                sourceTexture else swapchain.texture()
            val framebuffer = if (isLastIndex)
                Framebuffer.Display else swapchain.framebuffer()

            renderLayer(
                layer,
                texture,
                framebuffer,
                isLastIndex,
                isFirstIndex,
            )

            swapchain.next()
        }
    }

    private fun renderLayer(
        layer: Layer,
        texture: Texture,
        framebuffer: Framebuffer,
        isFrontBuffer: Boolean,
        isInverted: Boolean,
    ) {
        val quad = this.quad ?: return
        val viewportDimension = this.viewportDimension ?: return
        val swapchainDimension = this.swapchainDimension ?: return
        val shaderFactory = this.shaderFactory ?: return

        val shader = getShaderForLayer(layer, shaderFactory)
        val dimension = if (isFrontBuffer) viewportDimension else swapchainDimension

        // Change viewport based on mode
        GLES30.glViewport(
            0,
            0,
            dimension.width,
            dimension.height,
        )

        // Make shader active
        GLES30.glUseProgram(shader.program)

        // Bind uAspectRatio
        GLES30.glUniformMatrix4fv(
            Shader.uAspectRatio,
            1,
            false,
            if (isFrontBuffer) aspectRatioMatrix.values else unitMatrix.values,
            0,
        )

        // Bind uInvert
        GLES30.glUniformMatrix4fv(
            Shader.uInvert,
            1,
            false,
            if (isInverted) invertMatrix.values else unitMatrix.values,
            0,
        )

        // Bind uImage
        GLES30.glUniform1i(Shader.uImage, 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        texture.bind()

        // Bind uFactor
        GLES30.glUniform1f(Shader.uIntensity, layer.intensity.coerceIn(0.0f, 1.0f))

        // Bind layer variables
        layer.bind()

        // Bind render target
        framebuffer.bind()

        // Rendering
        quad.draw()
    }

    private fun getShaderForLayer(layer: Layer, shaderFactory: ShaderFactory): Shader {
        return layerShaderMap[layer::class] ?: run {
            val key = layer::class
            val shader = shaderFactory.generate(layer.source)
            layer.create(shader.program)
            layerShaderMap[key] = shader
            shader
        }
    }

    private fun updateDimensions() {
        val bitmapDimension = this.bitmapDimension ?: return
        val viewportDimension = this.viewportDimension ?: return

        val swapchainDimension = if (isPreview)
            bitmapDimension.fitIn(viewportDimension) else bitmapDimension

        previewSwapchain?.release()
        previewSwapchain = Swapchain.obtain(2, swapchainDimension.width, swapchainDimension.height)

        this.swapchainDimension = swapchainDimension

        if (isPreview) {
            val bitmapAspectRatio = bitmapDimension.aspectRatio
            val viewportAspectRatio = viewportDimension.aspectRatio

            aspectRatioMatrix
                .unit()
                .scale(
                    if (bitmapAspectRatio > viewportAspectRatio)
                        1.0f else (bitmapAspectRatio / viewportAspectRatio),
                    if (bitmapAspectRatio < viewportAspectRatio)
                        1.0f else (viewportAspectRatio / bitmapAspectRatio),
                    1.0f,
                )
        }
    }

}