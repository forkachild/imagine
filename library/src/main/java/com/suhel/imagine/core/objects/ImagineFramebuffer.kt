package com.suhel.imagine.core.objects

import android.opengl.GLES20
import androidx.annotation.VisibleForTesting
import com.suhel.imagine.util.getProxyInt
import com.suhel.imagine.util.setProxyInt

internal class ImagineFramebuffer @VisibleForTesting constructor(
    private val handle: Int,
) {

    private var isReleased: Boolean = false

    fun attachTexture(texture: ImagineTexture) {
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

        val default = ImagineFramebuffer(0)

        fun obtain(): ImagineFramebuffer {
            val framebufferHandle = getProxyInt {
                GLES20.glGenFramebuffers(1, it, 0)
            }

            return ImagineFramebuffer(framebufferHandle)
        }

        fun obtain(count: Int): List<ImagineFramebuffer> {
            val framebufferHandles = IntArray(count)
            GLES20.glGenFramebuffers(count, framebufferHandles, 0)

            return framebufferHandles.map { ImagineFramebuffer(it) }
        }

    }

}