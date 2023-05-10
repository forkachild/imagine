package com.suhel.imagine.core.objects

import android.opengl.GLES20
import androidx.annotation.VisibleForTesting
import com.suhel.imagine.util.getProxyInt
import com.suhel.imagine.util.setProxyInt

/**
 * A wrapper for an OpenGL Framebuffer Object with allocation safety.
 *
 * @property handle Reference to the underlying Framebuffer Object
 */
internal class ImagineFramebuffer @VisibleForTesting constructor(
    private val handle: Int,
) {

    /**
     * Indicates whether this underlying resource is released
     */
    private var isReleased: Boolean = false

    /**
     * Utility function to execute the block passed only if
     * the underlying resource is not released
     *
     * @param block Lambda to be executed safely
     */
    private fun releaseSafe(block: () -> Unit) {
        if (!isReleased) block()
    }

    /**
     * Attach a texture as a backing memory to this Framebuffer
     *
     * @param texture [ImagineTexture] to be attached
     */
    fun attachTexture(texture: ImagineTexture) = releaseSafe {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, handle)

        // Attach the texture handle to color attachment index 0
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            texture.handle,
            0
        )
    }

    /**
     * Release the underlying Framebuffer Object from memory
     */
    fun release() = releaseSafe {
        setProxyInt(handle) {
            GLES20.glDeleteFramebuffers(1, it, 0)
        }

        isReleased = true
    }

    /**
     * Bind this Framebuffer Object during drawing
     */
    fun bind() = releaseSafe {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, handle)
    }

    companion object {

        /**
         * The default Framebuffer Object representing the viewport
         */
        val default = ImagineFramebuffer(0)

        /**
         * Safely create a new Framebuffer Object in OpenGL
         * and wrap it in [ImagineFramebuffer]
         *
         * @return An active [ImagineFramebuffer]
         */
        fun create(): ImagineFramebuffer {
            val framebufferHandle = getProxyInt {
                GLES20.glGenFramebuffers(1, it, 0)
            }

            return ImagineFramebuffer(framebufferHandle)
        }

        /**
         * Safely create a number of Framebuffer Objects in
         * OpenGL and wrap them in [ImagineFramebuffer]
         *
         * @param count Number of Framebuffer Objects to be created
         *
         * @return List of active [ImagineFramebuffer]s
         */
        fun create(count: Int): List<ImagineFramebuffer> {
            val framebufferHandles = IntArray(count)
            GLES20.glGenFramebuffers(count, framebufferHandles, 0)

            return framebufferHandles.map { ImagineFramebuffer(it) }
        }

    }

}