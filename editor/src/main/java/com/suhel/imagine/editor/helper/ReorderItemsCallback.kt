package com.suhel.imagine.editor.helper

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ReorderItemsCallback(
    private val onItemMove: (Int, Int) -> Boolean,
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    0,
) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = onItemMove(viewHolder.adapterPosition, target.adapterPosition)

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Do nothing
    }

    override fun isLongPressDragEnabled(): Boolean = false

    override fun isItemViewSwipeEnabled(): Boolean = false

}