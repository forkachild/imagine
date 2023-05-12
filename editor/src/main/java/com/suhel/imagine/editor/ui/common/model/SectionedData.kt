package com.suhel.imagine.editor.ui.common.model

data class SectionedData<T>(
    val title: String,
    val items: List<T>,
)