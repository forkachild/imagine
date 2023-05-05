package com.suhel.imagine.core

import android.opengl.GLES20
import androidx.annotation.VisibleForTesting
import com.suhel.imagine.Constants
import com.suhel.imagine.util.getProxyInt
import com.suhel.imagine.util.setProxyInt

class Framebuffer @VisibleForTesting constructor(
    private val handle: Int = Constants.Resources.INVALID_HANDLE
) {

    private var isReleased: Boolean = false

    fun attachTexture(texture: Texture) {
        throwIfReleased()
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, handle)
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            texture.handle,
            0
        )
    }

    fun bind() {
        throwIfReleased()
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, handle)
    }

    fun release() {
        throwIfReleased()
        setProxyInt(handle) {
            GLES20.glDeleteFramebuffers(1, it, 0)
        }

        isReleased = true
    }

    private fun throwIfReleased() {
        if (isReleased) throw IllegalStateException("Framebuffer($handle) released")
    }

    companion object {

        val default = Framebuffer(0)

        fun obtain(): Framebuffer {
            val framebufferHandle = getProxyInt {
                GLES20.glGenFramebuffers(1, it, 0)
            }

            return Framebuffer(framebufferHandle)
        }

        fun obtain(count: Int): List<Framebuffer> {
            val framebufferHandles = IntArray(count)
            GLES20.glGenFramebuffers(count, framebufferHandles, 0)

            return framebufferHandles.map { Framebuffer(it) }
        }

    }

}