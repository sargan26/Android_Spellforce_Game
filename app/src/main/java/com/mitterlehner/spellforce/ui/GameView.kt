package com.mitterlehner.spellforce.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.mitterlehner.spellforce.game.Board
import com.mitterlehner.spellforce.game.TerrainType

class GameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val board = Board(14,8);
    private var selectedCell: Pair<Int, Int>? = null
    private val paint = Paint();
    private val cellSize: Float
        get() = width / board.cols.toFloat() // Dynamically calculate cell size

    init {
        board.initialize()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawGrid(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        for (row in 0 until board.rows) {
            for (col in 0 until board.cols) {
                val cell = board.grid[row][col]
                paint.color = when (cell.terrain) {
                    TerrainType.GRASS -> Color.GREEN
                    TerrainType.MOUNTAIN -> Color.GRAY
                    TerrainType.WATER -> Color.BLUE
                    TerrainType.MONUMENT -> Color.YELLOW
                }
                if (selectedCell == Pair(row, col)) {
                    paint.color = Color.RED
                }
                // Draw the cell rectangle
                canvas.drawRect(
                    col * cellSize,
                    row * cellSize,
                    (col + 1) * cellSize,
                    (row + 1) * cellSize,
                    paint
                )
                paint.style = Paint.Style.FILL // Reset style
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val col = (event.x / cellSize).toInt()
            val row = (event.y / cellSize).toInt()
            if (row in 0 until board.rows && col in 0 until board.cols) {
                val cell = board.grid[row][col]
                selectedCell = Pair(row, col)
                invalidate() // Redraw the view
            }
        }
        return true
    }

}