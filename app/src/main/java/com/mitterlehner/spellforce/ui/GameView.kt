package com.mitterlehner.spellforce.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.mitterlehner.spellforce.MainActivity
import com.mitterlehner.spellforce.R
import com.mitterlehner.spellforce.game.Board
import com.mitterlehner.spellforce.game.GameState
import com.mitterlehner.spellforce.game.GridCell
import com.mitterlehner.spellforce.game.OwnerTyp
import com.mitterlehner.spellforce.game.Swordsman
import com.mitterlehner.spellforce.game.Unit
import com.mitterlehner.spellforce.game.TerrainType

class GameView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val mainActivity: MainActivity
        get() = context as MainActivity

    val board: Board by lazy {
        Board(14, 9).apply {
            initialize()
        }
    }

    private var selectedCell: Pair<Int, Int>? = null
        get() = field
    private var selectedUnitCell: Pair<Int, Int>? = null
    val highlightedCells = mutableListOf<Pair<Int, Int>>()
    val attackRangeCells = mutableListOf<Pair<Int, Int>>()
    val player = mainActivity.player;
    var callback: GameViewCallback? = null
    private val highlightPaint = Paint().apply {
        color = Color.YELLOW
        alpha = 128 // Semi-transparent
        style = Paint.Style.FILL
    }
    private val attackRangePaint = Paint().apply {
        color = Color.RED
        alpha = 128 // Semi-transparent
        style = Paint.Style.FILL
    }

    private var attackEffectCell: Pair<Int, Int>? = null
    private var attackEffectTimer: Long = 0
    private val attackEffectDuration = 1000L // Duration in milliseconds (e.g. one second)


    // Terrain-Pictures
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
    private val damageBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.damage)


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

    private val attackHandler = android.os.Handler()
    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (attackEffectCell != null) {
                invalidate()
                attackHandler.postDelayed(this, 16L) // 16ms for ~60 FPS
            }
        }
    }

    private var movingUnit: Unit? = null
    private var movingFrom: Pair<Int, Int>? = null
    private var movingTo: Pair<Int, Int>? = null
    private var animationProgress: Float = 0f
    private val animationSpeed = 0.1f // Speed of the motion (0.1 = 10% per frame)

    // Handler for the animation
    private val animationHandler = android.os.Handler()
    private val animationRunnable = object : Runnable {
        override fun run() {
            if (animationProgress < 1f) {
                animationProgress += animationSpeed
                invalidate()
                animationHandler.postDelayed(this, 16L) // ~60 FPS
            } else {
                completeMove()
            }
        }
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
                // Draw the board
                canvas.drawBitmap(
                    bitmap,
                    null, // Source: Whole picture
                    getCellRect(col, row), // Des: Rectangle of field
                    null // No special paint needed
                )

                // Draw units
                cell.unit?.let { unit ->
                    val bitmap = when (unit) {
                        is Swordsman -> when (unit.owner) {
                            OwnerTyp.RED -> swordsmanBitmapRed
                            OwnerTyp.BLUE -> swordsmanBitmapBlue
                            else -> null
                        }
                        else -> null
                    }
                    bitmap?.let {
                        canvas.drawBitmap(
                            it,
                            null,
                            getCellRect(col, row),
                            null
                        )
                    }
                }


                // Draw unit during moving animation
                if (movingUnit != null && movingFrom != null && movingTo != null) {
                    val (fromRow, fromCol) = movingFrom!!
                    val (toRow, toCol) = movingTo!!

                    // Interpolate position
                    val interpolatedX = fromCol + (toCol - fromCol) * animationProgress
                    val interpolatedY = fromRow + (toRow - fromRow) * animationProgress

                    val interpolatedRect = android.graphics.RectF(
                        interpolatedX * cellSize,
                        interpolatedY * cellSize,
                        (interpolatedX + 1) * cellSize,
                        (interpolatedY + 1) * cellSize
                    )

                    // Draw unit
                    val bitmap = if (movingUnit!!.owner == OwnerTyp.BLUE) swordsmanBitmapBlue else swordsmanBitmapRed
                    canvas.drawBitmap(bitmap, null, interpolatedRect, null)
                }

                // Draw attack effect
                attackEffectCell?.let { (row, col) ->
                    if (System.currentTimeMillis() - attackEffectTimer < attackEffectDuration) {
                        canvas.drawBitmap(
                            damageBitmap,
                            null,
                            getCellRect(col, row),
                            null)
                    } else {
                        // Remove attack effect when finished
                        attackEffectCell = null
                    }
                }

                // Draw highlighted moving cells
                if (highlightedCells.contains(Pair(row, col))) {
                    canvas.drawRect(getCellRect(col, row), highlightPaint)
                }

                // Draw highlighted attack range cells
                if (attackRangeCells.contains(Pair(row, col))) {
                    canvas.drawRect(getCellRect(col, row), attackRangePaint)
                }

                // Draw selection indicator for the selected cell
                if (selectedCell == Pair(row, col)) {
                    drawCornerMarks(canvas, col, row)
                }
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
        invalidate()
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
        invalidate()
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
        val cornerLength = cellSize / 5 // Length of the red lines

        // Upper left
        canvas.drawLine(startX, startY, startX + cornerLength, startY, cornerPaint)
        canvas.drawLine(startX, startY, startX, startY + cornerLength, cornerPaint)

        // Upper right
        canvas.drawLine(endX, startY, endX - cornerLength, startY, cornerPaint)
        canvas.drawLine(endX, startY, endX, startY + cornerLength, cornerPaint)

        // Bottom left
        canvas.drawLine(startX, endY, startX + cornerLength, endY, cornerPaint)
        canvas.drawLine(startX, endY, startX, endY - cornerLength, cornerPaint)

        // Bottom right
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
                originCell.unit?.hasMoved = true
                startMoveAnimation(Pair(originRow, originCol), Pair(cell.row, cell.col), originCell.unit!!)
                originCell.unit = null
                callback?.onUnitSelected(cell.unit)

                // Overtake house
                if (cell.terrain == TerrainType.HOUSE && cell.buildingOwner != OwnerTyp.BLUE) {
                    cell.buildingOwner = OwnerTyp.BLUE
                    player.houseCount++
                    player.updateIncome()
                    callback?.updateIncome(player.currentIncome)
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

                // Start attack effect
                attackEffectCell = Pair(cell.row, cell.col)
                attackEffectTimer = System.currentTimeMillis()

                // Start attack effect handler
                attackHandler.post(refreshRunnable)

                invalidate()
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

    fun spawnEnemyUnit() {
        val enemyMonumentCell = board.grid[0][4]
        if (enemyMonumentCell.unit == null) {
            val swordsman = Swordsman().apply { owner = OwnerTyp.RED }
            enemyMonumentCell.unit = swordsman
            invalidate()
        }
    }

    fun handleEnemyTurn() {
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

        // Find all reachable cells
        val reachableCells = getReachableCells(fromCell.row, fromCell.col, movementRange)

        // Select be nearest cell to enemy
        val bestCell = reachableCells.minByOrNull { manhattanDistance(it, targetCell) }

        // Moving
        if (bestCell != null && bestCell.unit == null && bestCell.terrain != TerrainType.WATER) {
            startMoveAnimation(
                from = Pair(fromCell.row, fromCell.col),
                to = Pair(bestCell.row, bestCell.col),
                unit = unit
            )
            fromCell.unit = null
            unit.hasMoved = true
        }
    }

    // Gets all reachable cells from source unit
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
        return manhattanDistance(fromCell, toCell) == fromCell.unit?.attackRange
    }

    private fun attackUnit(attackerCell: GridCell, defenderCell: GridCell) {
        val attacker = attackerCell.unit ?: return
        val defender = defenderCell.unit ?: return

        defender.currentHealth -= attacker.attack
        if (defender.currentHealth <= 0) {
            defenderCell.unit = null
        }

        // Activate attack effect
        attackEffectCell = Pair(defenderCell.row, defenderCell.col)
        attackEffectTimer = System.currentTimeMillis()

        // Start attack effect handler
        attackHandler.post(refreshRunnable)

        invalidate()
    }

    private fun updateUnitStatus(unit: Unit?) {
        if (unit != null) {
            callback?.onUnitSelected(unit)
        }
    }



    private fun startMoveAnimation(from: Pair<Int, Int>, to: Pair<Int, Int>, unit: Unit) {
        movingUnit = unit
        movingFrom = from
        movingTo = to
        animationProgress = 0f
        animationHandler.post(animationRunnable)
    }

    private fun completeMove() {
        if (movingUnit != null && movingFrom != null && movingTo != null) {
            val (fromRow, fromCol) = movingFrom!!
            val (toRow, toCol) = movingTo!!

            // Finish movement of unit
            board.grid[toRow][toCol].unit = movingUnit
            board.grid[fromRow][fromCol].unit = null

            // Reset Animation
            movingUnit = null
            movingFrom = null
            movingTo = null
            animationProgress = 0f

            invalidate()
        }
    }

    interface GameViewCallback {
        fun updateGoldAmount(gold: Int)
        fun updateIncome(income: Int)
        fun onUnitSelected(unit: Unit?)
    }
}