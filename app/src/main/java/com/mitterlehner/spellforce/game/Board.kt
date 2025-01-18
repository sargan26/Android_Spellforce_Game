package com.mitterlehner.spellforce.game

class Board(val rows: Int, val cols: Int) {
    val grid: Array<Array<GridCell>> = Array(rows) { row ->
        Array(cols) { col ->
            GridCell(row, col, TerrainType.GRASS, false)
        }
    }

    fun initialize() {
        grid[0][0].terrain = TerrainType.MOUNTAIN
    }

}
