package com.suhel.imagine.core

import android.graphics.Bitmap
import android.opengl.GLES30
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.suhel.imagine.types.Dimension
import com.suhel.imagine.types.Layer
import com.suhel.imagine.types.Mat4
import com.suhel.imagine.util.ShaderFactory

class Engine {

    var isPreview: Boolean = true

    var layers: List<Layer> = emptyList()
        set(value) {
            updateShaders(field, value)
            field = value
        }

    private val quad: Quad = Quad()
    private val shaderFactory: ShaderFactory = ShaderFactory()
    private val layerShaderMap: MutableMap<Layer, Shader> = mutableMapOf()
    private val aspectRatioMatrix: Mat4 = Mat4()
    private val invertMatrix: Mat4 = Mat4.ofScale(1.0f, -1.0f, 1.0f)
    private val unitMatrix: Mat4 = Mat4()

    private var sourceTexture: Texture? = null
    private var swapchain: Swapchain? = null
    private var viewportDimension: Dimension = Dimension()
    private var bitmapDimension: Dimension = Dimension()
    private var swapchainDimension: Dimension = Dimension()

    fun allocate() {
        quad.allocate()
        shaderFactory.allocate()
    }

    fun setViewportSize(width: Int, height: Int) {
        viewportDimension = Dimension(width, height)
        updateDimensions()
    }

    fun setBitmap(bitmap: Bitmap) {
        sourceTexture?.release()
        sourceTexture = Texture.obtain(bitmap, true)
        bitmapDimension = Dimension.fromBitmap(bitmap)
        updateDimensions()
    }

    fun render() {
        val swapchain = swapchain ?: return
        val sourceTexture = sourceTexture ?: return

        layers.forEachIndexed { index, effect ->
            val isFirstIndex = index == 0
            val isLastIndex = index == layers.lastIndex

            val texture = if (isFirstIndex)
                sourceTexture else swapchain.texture()
            val framebuffer = if (isLastIndex)
                Framebuffer.Display else swapchain.framebuffer()

            renderEffect(
                effect,
                texture,
                framebuffer,
                isLastIndex,
                isFirstIndex,
            )

            swapchain.next()
        }
    }

    fun release() {

    }

    private fun renderEffect(
        layer: Layer,
        texture: Texture,
        framebuffer: Framebuffer,
        isFrontBuffer: Boolean,
        isInverted: Boolean,
    ) {
        val shader = layerShaderMap[layer]
            ?: throw IllegalStateException("Shader not present")

        val dimension = if (isFrontBuffer)
            viewportDimension else swapchainDimension

        // Change viewport based on mode
        GLES30.glViewport(
            0,
            0,
            dimension.width,
            dimension.height,
        )

        // Make shader active
        shader.use()

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
        GLES30.glUniform1f(Shader.uFactor, layer.blend.coerceIn(0.0f, 1.0f))

        // Bind layer variables
        layer.bind()

        // Bind render target
        framebuffer.bind()

        // Rendering
        quad.draw(shader)
    }

    private fun updateShaders(oldLayers: List<Layer>, newLayers: List<Layer>) {
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldLayers.size

            override fun getNewListSize(): Int = newLayers.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldLayers[oldItemPosition] == newLayers[newItemPosition]

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                oldLayers[oldItemPosition] == newLayers[newItemPosition]

        }).dispatchUpdatesTo(object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
                repeat(count) { index ->
                    val layer = newLayers[index + position]
                    layerShaderMap[layer] = shaderFactory.generate(layer)
                }
            }

            override fun onRemoved(position: Int, count: Int) {
                repeat(count) { index ->
                    val layer = newLayers[index + position]
                    layerShaderMap.remove(layer)
                }
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {

            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {

            }

        })
    }

    private fun updateDimensions() {
        if (bitmapDimension.isInvalid)
            return

        if (isPreview && viewportDimension.isInvalid)
            return

        swapchainDimension = if (isPreview)
            bitmapDimension.fitIn(viewportDimension) else bitmapDimension

        swapchain?.release()
        swapchain = Swapchain.obtain(2, swapchainDimension.width, swapchainDimension.height)

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

    companion object {

        private const val TAG = "Engine"

    }

}