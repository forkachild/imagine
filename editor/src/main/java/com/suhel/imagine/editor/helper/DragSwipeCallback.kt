package com.suhel.imagine.editor.helper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.suhel.imagine.editor.R

class DragSwipeCallback(
    context: Context,
    private val onItemMove: (Int, Int) -> Boolean,
    private val onItemSwipe: (Int) -> Unit,
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    ItemTouchHelper.START or ItemTouchHelper.END,
) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            MaterialColors.getColor(
                context,
                com.google.android.material.R.attr.colorErrorContainer,
                Color.RED
            ) else Color.RED
        style = Paint.Style.FILL
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = onItemMove(viewHolder.adapterPosition, target.adapterPosition)

    override fun onSwiped(
        viewHolder: RecyclerView.ViewHolder,
        direction: Int
    ) = onItemSwipe(viewHolder.adapterPosition)

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView
            if (dX > 0.0f) {
                canvas.drawRect(
                    itemView.left.toFloat(),
                    itemView.top.toFloat(),
                    dX,
                    itemView.bottom.toFloat(),
                    paint
                )
            } else {
                canvas.drawRect(
                    itemView.right.toFloat() + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat(),
                    paint
                )
            }
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun isLongPressDragEnabled(): Boolean = false

}