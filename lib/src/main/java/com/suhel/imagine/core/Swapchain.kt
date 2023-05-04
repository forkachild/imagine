package com.suhel.imagine.core

import androidx.annotation.VisibleForTesting
import com.suhel.imagine.types.Dimension

class Swapchain @VisibleForTesting constructor(
    val dimension: Dimension,
    private val textures: List<Texture> = emptyList(),
    private val framebuffers: List<Framebuffer> = emptyList()
) {

    private var index: Int = 0
    private var isReleased: Boolean = false

    private val nextIndex: Int
        get() = (index + 1) % LENGTH

    val framebuffer: Framebuffer
        get() {
            throwIfReleased()
            return framebuffers[nextIndex]
        }

    val texture: Texture
        get() {
            throwIfReleased()
            return textures[index]
        }

    fun next() {
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

        fun create(dimension: Dimension): Swapchain {
            val textures = Texture.create(LENGTH, dimension)
            val framebuffers = Framebuffer.obtain(LENGTH)

            repeat(LENGTH) { index ->
                framebuffers[index].attachTexture(textures[index])
            }

            return Swapchain(dimension, textures, framebuffers)
        }

    }

}