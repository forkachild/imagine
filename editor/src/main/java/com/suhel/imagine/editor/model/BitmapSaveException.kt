package com.suhel.imagine.editor.model

sealed class BitmapSaveException(message: String?) : Exception(message) {
    class MediaStoreRecord : BitmapSaveException("Failed to create MediaStore record")
    class OutputStream : BitmapSaveException("Failed to create OutputStream")
    class Save : BitmapSaveException("Failed to save bitmap")
}