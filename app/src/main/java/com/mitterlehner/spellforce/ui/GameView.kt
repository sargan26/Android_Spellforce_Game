package com.mitterlehner.spellforce.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.mitterlehner.spellforce.MainActivity
import com.mitterlehner.spellforce.R
import com.mitterlehner.spellforce.game.Board
import com.mitterlehner.spellforce.game.OwnerTyp
import com.mitterlehner.spellforce.game.Swordsman
import com.mitterlehner.spellforce.game.TerrainType
import com.mitterlehner.spellforce.game.UnitType

class GameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val mainActivity: MainActivity
        get() = context as MainActivity

    public val board: Board by lazy {
        Board(14, 9).apply {
            initialize()
        }
    }

    private var selectedCell: Pair<Int, Int>? = null
        get() = field
    private var selectedUnitCell: Pair<Int, Int>? = null
    private val highlightedCells = mutableListOf<Pair<Int, Int>>()
    val player = mainActivity.player;
    var callback: GameViewCallback? = null
    private val highlightPaint = Paint().apply {
        color = Color.YELLOW
        alpha = 128 // Halbtransparent
        style = Paint.Style.FILL
    }


    // Terrain-Bilder
    private val grassBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.grass)
    private val mountainBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.mountain2)
    private val waterBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.water)
    private val monumentBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.monument)
    private val forestBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.forest3)
    private val houseBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.house2)
    private val roadBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.road)
    private val bridgeBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.bridge)
    private val swordsmanBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.swordman2)


    private val paint = Paint();
    private val cornerPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }
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
                val bitmap = when (cell.terrain) {
                    TerrainType.GRASS -> grassBitmap
                    TerrainType.MOUNTAIN -> mountainBitmap
                    TerrainType.WATER -> waterBitmap
                    TerrainType.MONUMENT -> monumentBitmap
                    TerrainType.FOREST -> forestBitmap
                    TerrainType.HOUSE -> houseBitmap
                    TerrainType.ROAD -> roadBitmap
                    TerrainType.BRIDGE -> bridgeBitmap
                }
                // Zeichnen des Bitmaps
                canvas.drawBitmap(
                    bitmap,
                    null, // Quelle: Das gesamte Bild
                    getCellRect(col, row), // Ziel: Rechteck des Feldes
                    null // Kein spezieller Paint benötigt
                )

                // Zeichnen des Einheiten-Bitmaps, wenn Einheit vorhanden
                cell.unit?.let { unit ->
                    when (unit) {
                        is Swordsman -> {
                            canvas.drawBitmap(
                                swordsmanBitmap,
                                null, // Quelle: Das gesamte Bild
                                getCellRect(col, row), // Ziel: Rechteck des Feldes
                                null // Kein spezieller Paint benötigt
                            )
                        }
                        else -> {
                            // Für andere Einheiten oder keine Aktion
                        }
                    }
                }

                // Zeichne hervorgehobene Zellen
                if (highlightedCells.contains(Pair(row, col))) {
                    canvas.drawRect(getCellRect(col, row), highlightPaint)
                }

                // Draw selection indicator for the selected cell
                if (selectedCell == Pair(row, col)) {
                    drawCornerMarks(canvas, col, row)
                }


                //paint.style = Paint.Style.FILL // Reset style
            }
        }
    }

    private fun highlightMovementRange(row: Int, col: Int, range: Int) {
        highlightedCells.clear()
        for (r in (row - range)..(row + range)) {
            for (c in (col - range)..(col + range)) {
                if (r in 0 until board.rows && c in 0 until board.cols) {
                    val distance = Math.abs(row - r) + Math.abs(col - c)
                    val cell = board.grid[r][c]
                    if (distance <= range && cell.terrain != TerrainType.WATER) {
                        highlightedCells.add(Pair(r, c))
                    }
                }
            }
        }
        invalidate() // Redraw the view
    }


    private fun getCellRect(col: Int, row: Int) =
        android.graphics.RectF(
            col * cellSize,
            row * cellSize,
            (col + 1) * cellSize,
            (row + 1) * cellSize
        )

    private fun drawCornerMarks(canvas: Canvas, col: Int, row: Int) {
        val startX = col * cellSize
        val startY = row * cellSize
        val endX = (col + 1) * cellSize
        val endY = (row + 1) * cellSize
        val cornerLength = cellSize / 5 // Länge der roten Linien

        // Oben links
        canvas.drawLine(startX, startY, startX + cornerLength, startY, cornerPaint)
        canvas.drawLine(startX, startY, startX, startY + cornerLength, cornerPaint)

        // Oben rechts
        canvas.drawLine(endX, startY, endX - cornerLength, startY, cornerPaint)
        canvas.drawLine(endX, startY, endX, startY + cornerLength, cornerPaint)

        // Unten links
        canvas.drawLine(startX, endY, startX + cornerLength, endY, cornerPaint)
        canvas.drawLine(startX, endY, startX, endY - cornerLength, cornerPaint)

        // Unten rechts
        canvas.drawLine(endX, endY, endX - cornerLength, endY, cornerPaint)
        canvas.drawLine(endX, endY, endX, endY - cornerLength, cornerPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val col = (event.x / cellSize).toInt()
            val row = (event.y / cellSize).toInt()
            if (row in 0 until board.rows && col in 0 until board.cols) {
                selectedCell = Pair(row, col)
                val cell = board.grid[row][col]

                if (selectedUnitCell != null) {
                    // Update unit status
                    callback?.onUnitSelected(cell.unit)
                }


                // Prüfen, ob eine Einheit ausgewählt wurde und das Ziel hervorgehoben ist
                if (selectedUnitCell != null && highlightedCells.contains(selectedCell)) {
                    val (originRow, originCol) = selectedUnitCell!!
                    val originCell = board.grid[originRow][originCol]

                    // Bewege die Einheit, wenn sie nicht bereits bewegt wurde
                    if (originCell.unit?.hasMoved == false) {
                        cell.unit = originCell.unit
                        originCell.unit = null
                        cell.unit?.hasMoved = true

                        // Reset Hervorhebungen und Auswahl
                        highlightedCells.clear()
                        selectedUnitCell = null
                        invalidate()
                        return true
                    }
                }

                if (cell.unit != null && cell.unit!!.owner == OwnerTyp.BLUE
                    && cell.unit?.hasMoved == false) {
                    // Highlight movement range für ausgewählte Einheit
                    highlightMovementRange(row, col, cell.unit!!.movementRange)
                    selectedUnitCell = Pair(row, col)
                } else {
                    // Deselect und entferne Highlights
                    highlightedCells.clear()
                    selectedUnitCell = null
                    invalidate()
                }

                if (cell.terrain == TerrainType.MONUMENT &&
                    cell.buildingOwner == OwnerTyp.BLUE &&
                    cell.unit == null
                ) {
                    if (player.gold >= 150) {
                        val swordsman = Swordsman()
                        swordsman.owner = OwnerTyp.BLUE
                        cell.unit = swordsman
                        player.gold -= 150
                        callback?.updateGoldAmount(player.gold)
                        Log.d("GameDebug", "Swordsman placed at ($row, $col). Gold: ${player.gold}")
                        invalidate() // Redraw only after state change
                    }
                } else {
                    Log.d("GameDebug", "Cannot place Swordsman. Unit: ${cell.unit}")
                }
            }
        }
        return true
    }


    interface GameViewCallback {
        fun updateGoldAmount(gold: Int)
        fun onUnitSelected(unit: Unit?)
    }







}