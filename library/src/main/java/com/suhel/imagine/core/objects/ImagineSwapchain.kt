package com.suhel.imagine.core.objects

import android.graphics.Bitmap
import android.opengl.GLES20
import androidx.annotation.VisibleForTesting
import com.suhel.imagine.core.types.ImagineDimensions
import java.nio.ByteBuffer

internal class ImagineSwapchain @VisibleForTesting constructor(
    val dimensions: ImagineDimensions,
    private val textures: List<ImagineTexture> = emptyList(),
    private val framebuffers: List<ImagineFramebuffer> = emptyList()
) {

    private var index: Int = 0
    private var isReleased: Boolean = false

    private val nextIndex: Int
        get() = (index + 1) % LENGTH

    val framebuffer: ImagineFramebuffer
        get() {
            throwIfReleased()
            return framebuffers[nextIndex]
        }

    val texture: ImagineTexture
        get() {
            throwIfReleased()
            return textures[index]
        }

    val bitmap: Bitmap
        get() = ByteBuffer
            .allocateDirect(dimensions.width * dimensions.height * 4)
            .let { buffer ->
                GLES20.glReadPixels(
                    0,
                    0,
                    dimensions.width,
                    dimensions.height,
                    GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE,
                    buffer
                )

                Bitmap.createBitmap(
                    dimensions.width,
                    dimensions.height,
                    Bitmap.Config.ARGB_8888
                ).apply {
                    copyPixelsFromBuffer(buffer)
                }
            }

    fun swap() {
        index = nextIndex
    }

    fun release() {
        throwIfReleased()
        repeat(LENGTH) { index ->
            framebuffers[index].release()
            textures[index].release()
        }

        isReleased = true
    }

    private fun throwIfReleased() {
        if (isReleased) throw IllegalStateException("Swapchain released")
    }

    companion object {

        private const val LENGTH: Int = 2

        fun create(dimensions: ImagineDimensions): ImagineSwapchain {
            val textures = ImagineTexture.create(LENGTH, dimensions)
            val framebuffers = ImagineFramebuffer.obtain(LENGTH)

            repeat(LENGTH) { index ->
                framebuffers[index].attachTexture(textures[index])
            }

            return ImagineSwapchain(dimensions, textures, framebuffers)
        }

    }

}