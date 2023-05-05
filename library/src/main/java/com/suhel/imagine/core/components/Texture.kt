package com.suhel.imagine.core.components

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import androidx.annotation.VisibleForTesting
import com.suhel.imagine.types.Dimension
import com.suhel.imagine.util.getProxyInt
import com.suhel.imagine.util.setProxyInt

internal class Texture @VisibleForTesting constructor(
    val handle: Int,
    val dimension: Dimension,
) {

    private var isReleased: Boolean = false

    fun bind() = releaseSafe {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle)
    }

    fun release() = releaseSafe {
        setProxyInt(handle) {
            GLES20.glDeleteTextures(1, it, 0)
        }

        isReleased = true
    }

    private fun <T> releaseSafe(block: () -> T?): T? = if (isReleased) null else block()

    companion object {

        fun create(dimension: Dimension): Texture {
            val textureHandle = getProxyInt { GLES20.glGenTextures(1, it, 0) }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
            configure(false)
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                dimension.width,
                dimension.height,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null
            )

            return Texture(textureHandle, dimension)
        }

        fun create(count: Int, dimension: Dimension): List<Texture> {
            val textureHandles = IntArray(count)
            GLES20.glGenTextures(count, textureHandles, 0)

            textureHandles.forEach { textureHandle ->
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
                configure(false)
                GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_RGBA,
                    dimension.width,
                    dimension.height,
                    0,
                    GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE,
                    null
                )
            }

            return textureHandles.map { Texture(it, dimension) }
        }

        fun create(
            bitmap: Bitmap,
            mipmap: Boolean = false,
            recycleBitmap: Boolean = false
        ): Texture {
            val textureHandle = getProxyInt { GLES20.glGenTextures(1, it, 0) }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
            configure(mipmap)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            if (mipmap) GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
            val dimension = Dimension(bitmap.width, bitmap.height)
            if (recycleBitmap) bitmap.recycle()

            return Texture(textureHandle, dimension)
        }

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