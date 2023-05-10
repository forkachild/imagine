package com.suhel.imagine.core.objects

import android.graphics.Bitmap
import android.opengl.GLES20
import androidx.annotation.VisibleForTesting
import com.suhel.imagine.core.objects.ImagineTosschain.Companion.create
import com.suhel.imagine.core.types.ImagineDimensions
import java.nio.ByteBuffer

/**
 * A construct used to maintain two framebuffers, each having identical textures
 * attached to their color attachments.
 *
 * You cannot directly instantiate this class. Instead use the companion
 * function [create] to do so.
 *
 * @property dimensions The [ImagineDimensions] of the underlying textures
 * @property textures List of valid [ImagineTexture]s
 * @property framebuffers List of valid [ImagineFramebuffer]s
 */
internal class ImagineTosschain @VisibleForTesting constructor(
    val dimensions: ImagineDimensions,
    private val textures: List<ImagineTexture>,
    private val framebuffers: List<ImagineFramebuffer>,
) {

    /**
     * Indicates whether this underlying resource is released
     */
    private var isReleased: Boolean = false

    /**
     * Index of the currently sampled texture
     */
    private var index: Int = 0

    /**
     * Index of the currently active framebuffer
     */
    private val nextIndex: Int
        get() = (index + 1) % TOSS_CHAIN_LENGTH

    /**
     * The [ImagineFramebuffer] to bind while rendering
     */
    val framebuffer: ImagineFramebuffer
        get() {
            throwIfReleased()
            return framebuffers[nextIndex]
        }

    /**
     * The [ImagineTexture] to use for sampling
     */
    val texture: ImagineTexture
        get() {
            throwIfReleased()
            return textures[index]
        }

    /**
     * Obtain a bitmap from the pixels represented in the currently
     * bound framebuffer
     */
    val bitmap: Bitmap
        get() = ByteBuffer
            .allocateDirect(dimensions.width * dimensions.height * COLOR_COMPONENT_COUNT)
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

    /**
     * Alternate the framebuffers to enable rendering the other way round
     */
    fun swap() {
        index = nextIndex
    }

    /**
     * Releases the resources associated with the construct
     */
    fun release() {
        throwIfReleased()
        repeat(TOSS_CHAIN_LENGTH) { index ->
            framebuffers[index].release()
            textures[index].release()
        }

        isReleased = true
    }

    /**
     * Utility function to throw an exception in case this is released
     */
    private fun throwIfReleased() {
        if (isReleased) throw IllegalStateException("Tosschain released")
    }

    companion object {

        /**
         * Since we will be tossing back and forth so 2
         */
        private const val TOSS_CHAIN_LENGTH: Int = 2

        /**
         * Each color component has 4 components (r, g, b, a)
         */
        private const val COLOR_COMPONENT_COUNT: Int = 4

        /**
         * Safely instantiate an [ImagineTosschain]
         *
         * @param dimensions The [ImagineDimensions] of each textures
         *
         * @return A valid [ImagineTosschain]
         */
        fun create(dimensions: ImagineDimensions): ImagineTosschain {
            val textures = ImagineTexture.create(TOSS_CHAIN_LENGTH, dimensions)
            val framebuffers = ImagineFramebuffer.create(TOSS_CHAIN_LENGTH)

            repeat(TOSS_CHAIN_LENGTH) { index ->
                framebuffers[index].attachTexture(textures[index])
            }

            return ImagineTosschain(dimensions, textures, framebuffers)
        }

    }

}