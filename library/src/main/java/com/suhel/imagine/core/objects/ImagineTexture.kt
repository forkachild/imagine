package com.suhel.imagine.core.objects

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import androidx.annotation.VisibleForTesting
import com.suhel.imagine.core.types.ImagineDimensions
import com.suhel.imagine.util.getProxyInt
import com.suhel.imagine.util.setProxyInt

/**
 * A high level wrapper for an OpenGL Texture Object.
 *
 * You cannot directly instantiate this. Instead use the companion
 * function family of [create] to do so.
 *
 * @property handle Raw handle to the underlying OpenGL Texture
 * @property dimensions Dimensions of the image of this texture
 */
internal class ImagineTexture @VisibleForTesting constructor(
    val handle: Int,
    val dimensions: ImagineDimensions,
) {

    /**
     * Indicates whether this underlying resource is released
     */
    private var isReleased: Boolean = false

    /**
     * Safely binds this texture to the [GLES20.GL_TEXTURE_2D] target
     */
    fun bind() = releaseSafe {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle)
    }

    /**
     * Safely releases the texture from memory
     */
    fun release() = releaseSafe {
        setProxyInt(handle) {
            GLES20.glDeleteTextures(1, it, 0)
        }

        isReleased = true
    }

    /**
     * Utility function to execute the block passed only if
     * the underlying resource is not released
     *
     * @param block Lambda to be executed safely
     */
    private fun releaseSafe(block: () -> Unit) {
        if (!isReleased) block()
    }

    companion object {

        /**
         * Safely instantiate a texture of custom dimensions
         *
         * @param dimensions The [ImagineDimensions] to use while creating
         * the texture
         *
         * @return A valid [ImagineTexture]
         */
        fun create(dimensions: ImagineDimensions): ImagineTexture {
            val textureHandle = getProxyInt { GLES20.glGenTextures(1, it, 0) }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
            configure(false)
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                dimensions.width,
                dimensions.height,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null
            )

            return ImagineTexture(textureHandle, dimensions)
        }

        /**
         * Safely instantiate more than one textures of a given dimension
         *
         * @param count Number of textures to instantiate
         * @param dimensions The [ImagineDimensions] to use while creating
         *
         * @return List of valid [ImagineTexture]s
         */
        fun create(count: Int, dimensions: ImagineDimensions): List<ImagineTexture> {
            val textureHandles = IntArray(count)
            GLES20.glGenTextures(count, textureHandles, 0)

            textureHandles.forEach { textureHandle ->
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
                configure(false)
                GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_RGBA,
                    dimensions.width,
                    dimensions.height,
                    0,
                    GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE,
                    null
                )
            }

            return textureHandles.map { ImagineTexture(it, dimensions) }
        }

        /**
         * Safely instantiate a texture from a bitmap
         *
         * @param bitmap Bitmap to copy pixels from
         * @param mipmap Should enable & generate mipmapping
         * @param recycleBitmap Should recycle the bitmap after data has been extracted
         *
         * @return A valid [ImagineTexture]
         */
        fun create(
            bitmap: Bitmap,
            mipmap: Boolean = false,
            recycleBitmap: Boolean = false
        ): ImagineTexture {
            val textureHandle = getProxyInt { GLES20.glGenTextures(1, it, 0) }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
            configure(mipmap)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            if (mipmap) GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
            val dimensions = ImagineDimensions(bitmap.width, bitmap.height)
            if (recycleBitmap) bitmap.recycle()

            return ImagineTexture(textureHandle, dimensions)
        }

        /**
         * Configure some common values in a bound texture
         *
         * @param mipmap Should enable mipmapping
         */
        private fun configure(mipmap: Boolean) {
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                if (mipmap) GLES20.GL_LINEAR_MIPMAP_LINEAR else GLES20.GL_LINEAR
            )
        }

    }

}