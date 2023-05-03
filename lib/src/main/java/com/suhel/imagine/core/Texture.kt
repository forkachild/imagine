package com.suhel.imagine.core

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import androidx.annotation.VisibleForTesting
import com.suhel.imagine.types.Dimension
import com.suhel.imagine.util.getProxyInt
import com.suhel.imagine.util.setProxyInt

class Texture @VisibleForTesting constructor(
    val handle: Int,
    val dimension: Dimension,
) {

    private var isReleased: Boolean = false

    fun bind() = releaseSafe {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, handle)
    }

    fun release() = releaseSafe {
        setProxyInt(handle) {
            GLES30.glDeleteTextures(1, it, 0)
        }

        isReleased = true
    }

    private fun <T> releaseSafe(block: () -> T?): T? = if (isReleased) null else block()

    companion object {

        fun create(dimension: Dimension): Texture {
            val textureHandle = getProxyInt { GLES30.glGenTextures(1, it, 0) }
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle)
            configure(false)
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                0,
                GLES30.GL_RGBA,
                dimension.width,
                dimension.height,
                0,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                null
            )

            return Texture(textureHandle, dimension)
        }

        fun create(count: Int, dimension: Dimension): List<Texture> {
            val textureHandles = IntArray(count)
            GLES30.glGenTextures(count, textureHandles, 0)

            textureHandles.forEach { textureHandle ->
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle)
                configure(false)
                GLES30.glTexImage2D(
                    GLES30.GL_TEXTURE_2D,
                    0,
                    GLES30.GL_RGBA,
                    dimension.width,
                    dimension.height,
                    0,
                    GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE,
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
            val textureHandle = getProxyInt { GLES30.glGenTextures(1, it, 0) }
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle)
            configure(mipmap)
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
            if (mipmap) GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
            val dimension = Dimension(bitmap.width, bitmap.height)
            if (recycleBitmap) bitmap.recycle()

            return Texture(textureHandle, dimension)
        }

        private fun configure(mipmap: Boolean) {
            GLES30.glTexParameteri(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_S,
                GLES30.GL_CLAMP_TO_EDGE
            )
            GLES30.glTexParameteri(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_WRAP_T,
                GLES30.GL_CLAMP_TO_EDGE
            )
            GLES30.glTexParameteri(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER,
                GLES30.GL_LINEAR
            )
            GLES30.glTexParameteri(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER,
                if (mipmap) GLES30.GL_LINEAR_MIPMAP_LINEAR else GLES30.GL_LINEAR
            )
        }

    }

}