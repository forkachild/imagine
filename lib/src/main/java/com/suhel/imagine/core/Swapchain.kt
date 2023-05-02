package com.suhel.imagine.core

import androidx.annotation.VisibleForTesting
import com.suhel.imagine.types.Dimension

class Swapchain @VisibleForTesting constructor(
    val count: Int,
    val dimension: Dimension,
    private val textures: List<Texture> = emptyList(),
    private val framebuffers: List<Framebuffer> = emptyList()
) {

    private var index: Int = 0
    private var isReleased: Boolean = false

    private val nextIndex: Int
        get() = (index + 1) % count

    fun texture(): Texture {
        throwIfReleased()
        return textures[index]
    }

    fun framebuffer(): Framebuffer {
        throwIfReleased()
        return framebuffers[nextIndex]
    }

    fun next() {
        index = nextIndex
    }

    fun release() {
        throwIfReleased()
        repeat(count) { index ->
            framebuffers[index].release()
            textures[index].release()
        }

        isReleased = true
    }

    private fun throwIfReleased() {
        if (isReleased) throw IllegalStateException("Swapchain released")
    }

    companion object {

        fun create(count: Int, dimension: Dimension): Swapchain {
            val textures = Texture.create(count, dimension)
            val framebuffers = Framebuffer.obtain(count)

            repeat(count) { index ->
                framebuffers[index].attachTexture(textures[index])
            }

            return Swapchain(count, dimension, textures, framebuffers)
        }

    }

}