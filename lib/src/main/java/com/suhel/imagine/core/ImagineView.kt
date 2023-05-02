package com.suhel.imagine.core

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import android.util.AttributeSet
import com.suhel.imagine.types.Dimension
import com.suhel.imagine.types.ImageProvider
import com.suhel.imagine.types.Layer
import com.suhel.imagine.types.Mat4
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ImagineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs), Renderer {

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

    private var state: State = State()

    init {
        setEGLContextClientVersion(3)
        debugFlags = DEBUG_CHECK_GL_ERROR or DEBUG_LOG_GL_CALLS
        setRenderer(this)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        state = State(
            isReady = true,
            shaderFactory = ShaderFactory.create(),
            quad = Quad.create(),
        )
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        val dimension = state.viewport?.dimension
        if (dimension != null && dimension.width == width && dimension.height == height) return

        state = state.copy(
            viewport = Viewport(Dimension(width, height)),
            isPendingSwapchainUpdate = true,
            isPendingAspectRatioMatrixUpdate = true,
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        update()
        renderLayers()
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
                    Swapchain.create(2, texture.dimension.fitInside(viewport.dimension))
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
        val shaderFactory = state.shaderFactory ?: return
        val quad = state.quad ?: return
        val aspectRatioMatrix = state.aspectRatioMatrix ?: return
        val swapchain = state.swapchain ?: return
        val image = state.image ?: return
        val viewport = state.viewport ?: return
        val layers = state.layers ?: return

        layers.forEachIndexed { index, layer ->
            val isFirstIndex = index == 0
            val isLastIndex = index == layers.lastIndex

            val texture = if (isFirstIndex) image else swapchain.texture()
            val framebuffer = if (isLastIndex) Framebuffer.Viewport else swapchain.framebuffer()

            renderLayer(
                layer,
                quad,
                viewport,
                swapchain,
                shaderFactory,
                texture,
                framebuffer,
                aspectRatioMatrix,
                isLastIndex,
                isFirstIndex,
            )
        }
    }

    private fun renderLayer(
        layer: Layer,
        quad: Quad,
        viewport: Viewport,
        swapchain: Swapchain,
        shaderFactory: ShaderFactory,
        texture: Texture,
        framebuffer: Framebuffer,
        aspectRatioMatrix: Mat4,
        isFrontBuffer: Boolean,
        isInverted: Boolean,
    ) {
        val shader = shaderFactory.getShader(layer) ?: return
        val dimension = if (isFrontBuffer) viewport.dimension else swapchain.dimension

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
            if (isFrontBuffer) aspectRatioMatrix.values else UnitMatrix.values,
            0,
        )

        // Bind uInvert
        GLES30.glUniformMatrix4fv(
            Shader.uInvert,
            1,
            false,
            if (isInverted) InvertMatrix.values else UnitMatrix.values,
            0,
        )

        // Bind uImage
        GLES30.glUniform1i(Shader.uImage, 0)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture.handle)

        // Bind uFactor
        GLES30.glUniform1f(Shader.uIntensity, layer.intensity.coerceIn(0.0f, 1.0f))

        // Bind layer variables
        layer.bind(shader.program)

        // Bind render target
        framebuffer.bind()

        // Rendering
        quad.draw()

        // Move on to the next image
        swapchain.next()
    }

    private data class State(
        val isReady: Boolean = false,
        val isPreview: Boolean = true,
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
    )

    companion object {
        val UnitMatrix: Mat4 = Mat4.ofUnit()
        val InvertMatrix: Mat4 = Mat4.ofScale(1.0f, -1.0f, 1.0f)
    }

}