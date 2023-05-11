package com.suhel.imagine.editor.ui.main

import android.view.View
import android.widget.AdapterView

class SpinnerSelectionListener(
    private val onSelected: (Int) -> Unit,
) : AdapterView.OnItemSelectedListener {

    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ) = onSelected(position)

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

}