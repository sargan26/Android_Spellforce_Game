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
import com.mitterlehner.spellforce.game.GridCell
import com.mitterlehner.spellforce.game.OwnerTyp
import com.mitterlehner.spellforce.game.Swordsman
import com.mitterlehner.spellforce.game.Unit
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
    public val highlightedCells = mutableListOf<Pair<Int, Int>>()
    public val attackRangeCells = mutableListOf<Pair<Int, Int>>()
    val player = mainActivity.player;
    var callback: GameViewCallback? = null
    private val highlightPaint = Paint().apply {
        color = Color.YELLOW
        alpha = 128 // Halbtransparent
        style = Paint.Style.FILL
    }
    private val attackRangePaint = Paint().apply {
        color = Color.RED
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
    private val houseBitmapBlue: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.house_blue2)
    private val roadBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.road)
    private val bridgeBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.bridge)
    private val swordsmanBitmapRed: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.swordman_red)
    private val swordsmanBitmapBlue: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.swordman_blue)


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
                    TerrainType.HOUSE -> if (cell.buildingOwner == OwnerTyp.BLUE) { houseBitmapBlue } else { houseBitmap }
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
                    val bitmap = when (unit) {
                        is Swordsman -> when (unit.owner) {
                            OwnerTyp.RED -> swordsmanBitmapRed
                            OwnerTyp.BLUE -> swordsmanBitmapBlue
                            else -> null // Optional: Kein Bitmap für andere Besitzer
                        }
                        else -> null // Optional: Kein Bitmap für andere Einheitstypen
                    }

                    // Zeichne die Einheit, wenn ein Bitmap ausgewählt wurde
                    bitmap?.let {
                        canvas.drawBitmap(
                            it,
                            null, // Quelle: Das gesamte Bild
                            getCellRect(col, row), // Ziel: Rechteck des Feldes
                            null // Kein spezieller Paint benötigt
                        )
                    }
                }


                // Zeichne hervorgehobene Zellen
                if (highlightedCells.contains(Pair(row, col))) {
                    canvas.drawRect(getCellRect(col, row), highlightPaint)
                }

                // Zeichne hervorgehobene Attack Range Zellen
                if (attackRangeCells.contains(Pair(row, col))) {
                    canvas.drawRect(getCellRect(col, row), attackRangePaint)
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

    private fun highlightAttackRange(row: Int, col: Int, range: Int) {
        attackRangeCells.clear()
        for (r in (row - range)..(row + range)) {
            for (c in (col - range)..(col + range)) {
                if (r in 0 until board.rows && c in 0 until board.cols) {
                    val distance = Math.abs(row - r) + Math.abs(col - c)
                    val cell = board.grid[r][c]
                    if (distance <= range && cell.terrain != TerrainType.WATER) {
                        attackRangeCells.add(Pair(r, c))
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

                if (tryMoveUnit(cell)) return true
                if (tryAttackUnit(cell)) return true
                if (trySelectUnit(cell, row, col)) return true
                if (tryPlaceSwordsman(cell, row, col)) return true
                updateUnitStatus(cell.unit)
            }
        }

        return true
    }

    private fun tryMoveUnit(cell: GridCell): Boolean {
        // Click on same unit = skip moving
        if (selectedUnitCell != null && highlightedCells.contains(selectedCell) && cell.unit != null) {
            cell.unit!!.hasMoved = true
            highlightedCells.clear()
            selectedUnitCell = null
            updateUnitStatus(cell.unit)
            invalidate()
            return true
        }

        // Moving
        if (selectedUnitCell != null && highlightedCells.contains(selectedCell) && cell.unit == null) {
            val (originRow, originCol) = selectedUnitCell!!
            val originCell = board.grid[originRow][originCol]

            if (originCell.unit?.hasMoved == false) {
                cell.unit = originCell.unit
                originCell.unit = null
                cell.unit?.hasMoved = true
                callback?.onUnitSelected(cell.unit)

                // Wenn die Einheit auf ein Haus zieht, ändere den Besitzer
                if (cell.terrain == TerrainType.HOUSE && cell.buildingOwner != OwnerTyp.BLUE) {
                    cell.buildingOwner = OwnerTyp.BLUE
                    player.houseCount++
                    player.updateIncome() // Einkommen aktualisieren
                    callback?.updateIncome(player.currentIncome)
                    //callback?.updateGoldAmount(player.gold)
                }

                highlightedCells.clear()
                selectedUnitCell = null
                updateUnitStatus(cell.unit)
                invalidate()
                return true
            }
        }
        return false
    }

    private fun tryAttackUnit(cell: GridCell): Boolean {
        if (selectedUnitCell != null && attackRangeCells.contains(selectedCell)
            && cell.unit != null) {
            val (originRow, originCol) = selectedUnitCell!!
            val originCell = board.grid[originRow][originCol]

            if (originCell.unit?.owner != cell.unit?.owner) {
                val attackValue = originCell.unit?.attack ?: 0
                cell.unit?.currentHealth = (cell.unit?.currentHealth ?: 0) - attackValue

                val targetHealth = cell.unit?.currentHealth ?: 0
                if (targetHealth <= 0) {
                    cell.unit = null
                    invalidate()
                }
                originCell.unit?.hasAttacked = true
            }

        }
        return false
    }

    private fun trySelectUnit(cell: GridCell, row: Int, col: Int): Boolean {
        // Move highlighting
        if (cell.unit != null && cell.unit!!.owner == OwnerTyp.BLUE && cell.unit?.hasMoved == false) {
            highlightMovementRange(row, col, cell.unit!!.movementRange)
            selectedUnitCell = Pair(row, col)
            updateUnitStatus(cell.unit)
            return true
        }
        // Attack highlighting
        if (cell.unit != null && cell.unit!!.owner == OwnerTyp.BLUE
            && cell.unit?.hasMoved == true && cell.unit?.hasAttacked == false) {
            highlightAttackRange(row, col, cell.unit!!.attackRange)
            selectedUnitCell = Pair(row, col)
            updateUnitStatus(cell.unit)
            return true
        }

        highlightedCells.clear()
        attackRangeCells.clear()
        selectedUnitCell = null
        updateUnitStatus(cell.unit)
        invalidate()
        return false
    }

    private fun tryPlaceSwordsman(cell: GridCell, row: Int, col: Int): Boolean {
        if (cell.terrain == TerrainType.MONUMENT && cell.buildingOwner == OwnerTyp.BLUE && cell.unit == null) {
            if (player.gold >= 150) {
                val swordsman = Swordsman()
                swordsman.owner = OwnerTyp.BLUE
                cell.unit = swordsman
                player.gold -= 150
                callback?.updateGoldAmount(player.gold)
                updateUnitStatus(cell.unit)
                invalidate()
                return true
            }
        }
        return false
    }

    public fun spawnEnemyUnit() {
        val enemyMonumentCell = board.grid[0][4]
        if (enemyMonumentCell.unit == null) {
            val swordsman = Swordsman().apply { owner = OwnerTyp.RED }
            enemyMonumentCell.unit = swordsman
            invalidate()
        }
    }

    public fun handleEnemyTurn() {
        println("handleEnemyTurn")

        for (row in board.grid) {
            for (cell in row) {
                val unit = cell.unit
                if (unit != null && unit.owner == OwnerTyp.RED && !unit.hasMoved) {
                    handleEnemyUnitAction(cell)
                }
            }
        }
    }

    private fun handleEnemyUnitAction(enemyCell: GridCell) {
        println("handleEnemyUnitAction")
        val enemyUnit = enemyCell.unit ?: return
        val targetCell = findNearestEnemy(enemyCell)

        if (targetCell != null) {
            moveUnit(enemyUnit, enemyCell, targetCell)
            if (isInAttackRange(enemyCell, targetCell)) {
                attackUnit(enemyCell, targetCell)
            }
        }
    }

    private fun findNearestEnemy(enemyCell: GridCell): GridCell? {
        println("findNearestEnemy")
        var nearestCell: GridCell? = null
        var shortestDistance = Int.MAX_VALUE

        for (row in board.grid) {
            for (cell in row) {
                if (cell.unit?.owner == OwnerTyp.BLUE) {
                    val distance = manhattanDistance(enemyCell, cell)
                    if (distance < shortestDistance) {
                        shortestDistance = distance
                        nearestCell = cell
                    }
                }
            }
        }

        return nearestCell
    }

    private fun manhattanDistance(cell1: GridCell, cell2: GridCell): Int {
        return Math.abs(cell1.row - cell2.row) + Math.abs(cell1.col - cell2.col)
    }

    fun moveUnit(unit: Unit, fromCell: GridCell, targetCell: GridCell) {
        val movementRange = unit.movementRange

        // Finde alle möglichen Zellen, in die sich die Einheit bewegen kann
        val reachableCells = getReachableCells(fromCell.row, fromCell.col, movementRange)

        // Wähle die Zelle aus, die dem Ziel am nächsten ist
        val bestCell = reachableCells.minByOrNull { manhattanDistance(it, targetCell) }

        if (bestCell != null && bestCell.unit == null && bestCell.terrain != TerrainType.WATER) {
            // Bewege die Einheit in die bestmögliche Zelle
            bestCell.unit = unit
            fromCell.unit = null
            unit.hasMoved = true
            println("Gegner bewegt sich von ${fromCell.row},${fromCell.col} zu ${bestCell.row},${bestCell.col}")
        }
    }

    // Hilfsmethode, um alle erreichbaren Zellen innerhalb der Bewegungsreichweite zu finden
    fun getReachableCells(row: Int, col: Int, range: Int): List<GridCell> {
        val reachableCells = mutableListOf<GridCell>()

        for (r in (row - range)..(row + range)) {
            for (c in (col - range)..(col + range)) {
                if (r in 0 until board.rows && c in 0 until board.cols) {
                    val cell = board.grid[r][c]
                    val distance = Math.abs(row - r) + Math.abs(col - c)
                    if (distance <= range) {
                        reachableCells.add(cell)
                    }
                }
            }
        }

        return reachableCells
    }

    private fun isInAttackRange(fromCell: GridCell, toCell: GridCell): Boolean {
        return manhattanDistance(fromCell, toCell) == 1 // Angriffsreichweite 1 Feld
    }

    private fun attackUnit(attackerCell: GridCell, defenderCell: GridCell) {
        println("attackUnit")
        val attacker = attackerCell.unit ?: return
        val defender = defenderCell.unit ?: return

        defender.currentHealth -= attacker.attack
        println("Gegner greift Einheit an! Schaden: ${attacker.attack}, Verbleibende Gesundheit: ${defender.currentHealth}")
        if (defender.currentHealth <= 0) {
            println("Einheit ${defender.name} wurde besiegt!")
            defenderCell.unit = null
        }
    }

    private fun updateUnitStatus(unit: Unit?) {
        if (unit != null) {
            callback?.onUnitSelected(unit)
        }
    }

    interface GameViewCallback {
        fun updateGoldAmount(gold: Int)
        fun updateIncome(income: Int)
        fun onUnitSelected(unit: Unit?)
    }







}