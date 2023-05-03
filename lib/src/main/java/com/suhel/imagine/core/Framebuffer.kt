package com.suhel.imagine.core

import android.opengl.GLES30
import androidx.annotation.VisibleForTesting
import com.suhel.imagine.Constants
import com.suhel.imagine.util.getProxyInt
import com.suhel.imagine.util.setProxyInt

class Framebuffer @VisibleForTesting constructor(
    val handle: Int = Constants.Resources.INVALID_HANDLE
) {

    private var isReleased: Boolean = false

    fun attachTexture(texture: Texture) {
        throwIfReleased()
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, handle)
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER,
            GLES30.GL_COLOR_ATTACHMENT0,
            GLES30.GL_TEXTURE_2D,
            texture.handle,
            0
        )
    }

    fun bind() {
        throwIfReleased()
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, handle)
    }

    fun clear() {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
    }

    fun release() {
        throwIfReleased()
        setProxyInt(handle) {
            GLES30.glDeleteFramebuffers(1, it, 0)
        }

        isReleased = true
    }

    private fun throwIfReleased() {
        if (isReleased) throw IllegalStateException("Framebuffer($handle) released")
    }

    companion object {

        val Viewport = Framebuffer(0)

        fun obtain(): Framebuffer {
            val framebufferHandle = getProxyInt {
                GLES30.glGenFramebuffers(1, it, 0)
            }

            return Framebuffer(framebufferHandle)
        }

        fun obtain(count: Int): List<Framebuffer> {
            val framebufferHandles = IntArray(count)
            GLES30.glGenFramebuffers(count, framebufferHandles, 0)

            return framebufferHandles.map { Framebuffer(it) }
        }

    }

}