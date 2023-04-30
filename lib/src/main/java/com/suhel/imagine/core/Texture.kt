package com.suhel.imagine.core

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import androidx.annotation.VisibleForTesting
import com.suhel.imagine.Constants
import com.suhel.imagine.util.getProxyInt
import com.suhel.imagine.util.setProxyInt

class Texture @VisibleForTesting constructor(
    val handle: Int = Constants.Resources.INVALID_HANDLE,
    val width: Int = Constants.Dimensions.INVALID_SIZE,
    val height: Int = Constants.Dimensions.INVALID_SIZE,
) {

    private var isReleased: Boolean = false

    fun bind() {
        throwIfReleased()
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, handle)
    }

    fun release() {
        throwIfReleased()
        setProxyInt(handle) {
            GLES30.glDeleteTextures(1, it, 0)
        }

        isReleased = true
    }

    private fun throwIfReleased() {
        if(isReleased) throw IllegalStateException("Texture($handle) released")
    }

    companion object {

        fun obtain(width: Int, height: Int): Texture {
            val textureHandle = getProxyInt { GLES30.glGenTextures(1, it, 0) }
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle)
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
                GLES30.GL_LINEAR
            )
            GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                0,
                GLES30.GL_RGBA,
                width,
                height,
                0,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                null
            )

            return Texture(textureHandle, width, height)
        }

        fun obtain(count: Int, width: Int, height: Int): List<Texture> {
            val textureHandles = IntArray(count)
            GLES30.glGenTextures(count, textureHandles, 0)

            textureHandles.forEach { textureHandle ->
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle)
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
                    GLES30.GL_LINEAR
                )
                GLES30.glTexImage2D(
                    GLES30.GL_TEXTURE_2D,
                    0,
                    GLES30.GL_RGBA,
                    width,
                    height,
                    0,
                    GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE,
                    null
                )
            }

            return textureHandles.map { Texture(it, width, height) }
        }

        fun obtain(bitmap: Bitmap, mipmap: Boolean = false): Texture {
            val textureHandle = getProxyInt { GLES30.glGenTextures(1, it, 0) }
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle)
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
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)

            if (mipmap) {
                GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
            }

            return Texture(textureHandle, bitmap.width, bitmap.height)
        }

    }

}