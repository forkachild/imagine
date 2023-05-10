package com.suhel.imagine.core.types

import android.opengl.Matrix
import androidx.annotation.VisibleForTesting

/**
 * Represents a 16-component Float matrix and facilitates common operations
 * on it
 */
class ImagineMatrix @VisibleForTesting constructor() {

    /**
     * The actual matrix
     */
    val data = FloatArray(16)

    init {
        // Always init it to identity to be safe
        Matrix.setIdentityM(data, 0)
    }

    /**
     * Scales the matrix along x, y and z axes
     */
    fun scale(x: Float, y: Float, z: Float): ImagineMatrix {
        Matrix.scaleM(data, 0, x, y, z)
        return this
    }

    companion object {

        /**
         * Instantiates an identity matrix
         *
         * @return An [ImagineMatrix]
         */
        fun ofIdentity(): ImagineMatrix {
            return ImagineMatrix()
        }

        /**
         * Instantiates a matrix scaled along x, y and z axes
         *
         * @param x X-axis value to scale
         * @param y Y-axis value to scale
         * @param z Z-axis value to scale
         *
         * @return An [ImagineMatrix]
         */
        fun ofScale(x: Float, y: Float, z: Float): ImagineMatrix {
            return ImagineMatrix().scale(x, y, z)
        }

        /**
         * Instantiates a matrix scaled across x and y axes to aspect correct
         * an image in its container. The z axis remains unit
         *
         * @param imageAspectRatio The aspect ratio in [Float] of the image
         * @param containerAspectRatio The aspect ratio in [Float] of the container
         *
         * @return An [ImagineMatrix]
         */
        fun ofAspectFit(
            imageAspectRatio: Float,
            containerAspectRatio: Float
        ): ImagineMatrix = ImagineMatrix().scale(
            if (imageAspectRatio > containerAspectRatio)
                1.0f else (imageAspectRatio / containerAspectRatio),
            if (imageAspectRatio < containerAspectRatio)
                1.0f else (containerAspectRatio / imageAspectRatio),
            1.0f,
        )

        /**
         * A ready made identity matrix
         */
        val identity = ofIdentity()

        /**
         * A ready made upside-down flip matrix
         */
        val invertY = ofScale(1.0f, -1.0f, 1.0f)

    }

}