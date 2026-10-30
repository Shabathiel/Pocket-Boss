package edu.umg.pocketboss

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Helper de swipe horizontal para eliminar items.
 * Muestra un fondo rojo con una "X" o icono mientras se desliza.
 */
fun crearSwipeEliminar(
    recycler: RecyclerView,
    onEliminar: (posicion: Int) -> Unit
): ItemTouchHelper {

    val bgPaint = Paint().apply {
        color = ContextCompat.getColor(recycler.context, R.color.gasto)
        isAntiAlias = true
    }
    val iconPaint = Paint().apply {
        color = ContextCompat.getColor(recycler.context, android.R.color.white)
        textSize = 60f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    val callback = object : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            onEliminar(viewHolder.bindingAdapterPosition)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val radius = 16f * recyclerView.resources.displayMetrics.density
            val margin = 8f * recyclerView.resources.displayMetrics.density

            val rect: RectF = when {
                dX > 0 -> RectF(
                    itemView.left + margin,
                    itemView.top.toFloat(),
                    itemView.left + dX,
                    itemView.bottom.toFloat()
                )
                dX < 0 -> RectF(
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right - margin,
                    itemView.bottom.toFloat()
                )
                else -> RectF()
            }

            if (dX != 0f) {
                c.drawRoundRect(rect, radius, radius, bgPaint)
                val centerX = rect.centerX()
                val centerY = itemView.top + itemView.height / 2f + 20f
                c.drawText("✕", centerX, centerY, iconPaint)
            }

            super.onChildDraw(
                c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
            )
        }
    }

    return ItemTouchHelper(callback).also { it.attachToRecyclerView(recycler) }
}