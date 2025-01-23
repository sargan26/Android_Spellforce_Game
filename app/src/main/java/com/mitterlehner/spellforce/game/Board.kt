package com.mitterlehner.spellforce.game

class Board(val rows: Int, val cols: Int) {
//    val grid: Array<Array<GridCell>> = Array(rows) { row ->
//        Array(cols) { col ->
//            GridCell(row, col, TerrainType.GRASS, OwnerTyp.NONE, null)
//        }
//    }
    lateinit var grid: Array<Array<GridCell>>

    fun initialize() {
        grid = Array(rows) { row ->
            Array(cols) { col ->
                GridCell(row, col, TerrainType.GRASS, OwnerTyp.NONE, null)
            }
        }

        // column 1
        grid[0][0].terrain = TerrainType.WATER
        grid[0][1].terrain = TerrainType.MOUNTAIN
        grid[0][2].terrain = TerrainType.MOUNTAIN
        grid[0][3].terrain = TerrainType.HOUSE
        grid[0][4].terrain = TerrainType.MONUMENT
        grid[0][4].buildingOwner = OwnerTyp.RED
        grid[0][5].terrain = TerrainType.FOREST
        grid[0][6].terrain = TerrainType.GRASS
        grid[0][7].terrain = TerrainType.FOREST
        grid[0][8].terrain = TerrainType.WATER

        // column 2
        grid[1][0].terrain = TerrainType.WATER
        grid[1][1].terrain = TerrainType.GRASS
        grid[1][2].terrain = TerrainType.HOUSE
        grid[1][3].terrain = TerrainType.GRASS
        grid[1][4].terrain = TerrainType.ROAD
        grid[1][5].terrain = TerrainType.GRASS
        grid[1][6].terrain = TerrainType.HOUSE
        grid[1][7].terrain = TerrainType.MOUNTAIN
        grid[1][8].terrain = TerrainType.WATER

        // column 3
        grid[2][0].terrain = TerrainType.WATER
        grid[2][1].terrain = TerrainType.WATER
        grid[2][2].terrain = TerrainType.WATER
        grid[2][3].terrain = TerrainType.FOREST
        grid[2][4].terrain = TerrainType.ROAD
        grid[2][5].terrain = TerrainType.GRASS
        grid[2][6].terrain = TerrainType.WATER
        grid[2][7].terrain = TerrainType.WATER
        grid[2][8].terrain = TerrainType.WATER

        // column 4
        grid[3][0].terrain = TerrainType.WATER
        grid[3][1].terrain = TerrainType.WATER
        grid[3][2].terrain = TerrainType.WATER
        grid[3][3].terrain = TerrainType.GRASS
        grid[3][4].terrain = TerrainType.ROAD
        grid[3][5].terrain = TerrainType.FOREST
        grid[3][6].terrain = TerrainType.WATER
        grid[3][7].terrain = TerrainType.WATER
        grid[3][8].terrain = TerrainType.WATER

        // column 5
        grid[4][0].terrain = TerrainType.WATER
        grid[4][1].terrain = TerrainType.FOREST
        grid[4][2].terrain = TerrainType.HOUSE
        grid[4][3].terrain = TerrainType.GRASS
        grid[4][4].terrain = TerrainType.ROAD
        grid[4][5].terrain = TerrainType.MOUNTAIN
        grid[4][6].terrain = TerrainType.HOUSE
        grid[4][7].terrain = TerrainType.MOUNTAIN
        grid[4][8].terrain = TerrainType.WATER

        // column 6
        grid[5][0].terrain = TerrainType.WATER
        grid[5][1].terrain = TerrainType.GRASS
        grid[5][2].terrain = TerrainType.ROAD
        grid[5][3].terrain = TerrainType.ROAD
        grid[5][4].terrain = TerrainType.ROAD
        grid[5][5].terrain = TerrainType.ROAD
        grid[5][6].terrain = TerrainType.ROAD
        grid[5][7].terrain = TerrainType.GRASS
        grid[5][8].terrain = TerrainType.WATER

        // column 7
        grid[6][0].terrain = TerrainType.WATER
        grid[6][1].terrain = TerrainType.FOREST
        grid[6][2].terrain = TerrainType.ROAD
        grid[6][3].terrain = TerrainType.WATER
        grid[6][4].terrain = TerrainType.BRIDGE
        grid[6][5].terrain = TerrainType.WATER
        grid[6][6].terrain = TerrainType.ROAD
        grid[6][7].terrain = TerrainType.GRASS
        grid[6][8].terrain = TerrainType.WATER

        // column 8
        grid[7][0].terrain = TerrainType.WATER
        grid[7][1].terrain = TerrainType.GRASS
        grid[7][2].terrain = TerrainType.ROAD
        grid[7][3].terrain = TerrainType.WATER
        grid[7][4].terrain = TerrainType.BRIDGE
        grid[7][5].terrain = TerrainType.WATER
        grid[7][6].terrain = TerrainType.ROAD
        grid[7][7].terrain = TerrainType.FOREST
        grid[7][8].terrain = TerrainType.WATER

        // column 9
        grid[8][0].terrain = TerrainType.WATER
        grid[8][1].terrain = TerrainType.GRASS
        grid[8][2].terrain = TerrainType.ROAD
        grid[8][3].terrain = TerrainType.ROAD
        grid[8][4].terrain = TerrainType.ROAD
        grid[8][5].terrain = TerrainType.ROAD
        grid[8][6].terrain = TerrainType.ROAD
        grid[8][7].terrain = TerrainType.GRASS
        grid[8][8].terrain = TerrainType.WATER

        // column 10
        grid[9][0].terrain = TerrainType.WATER
        grid[9][1].terrain = TerrainType.MOUNTAIN
        grid[9][2].terrain = TerrainType.HOUSE
        grid[9][3].terrain = TerrainType.GRASS
        grid[9][4].terrain = TerrainType.ROAD
        grid[9][5].terrain = TerrainType.GRASS
        grid[9][6].terrain = TerrainType.HOUSE
        grid[9][7].terrain = TerrainType.FOREST
        grid[9][8].terrain = TerrainType.WATER

        // column 11
        grid[10][0].terrain = TerrainType.WATER
        grid[10][1].terrain = TerrainType.WATER
        grid[10][2].terrain = TerrainType.WATER
        grid[10][3].terrain = TerrainType.GRASS
        grid[10][4].terrain = TerrainType.ROAD
        grid[10][5].terrain = TerrainType.FOREST
        grid[10][6].terrain = TerrainType.WATER
        grid[10][7].terrain = TerrainType.WATER
        grid[10][8].terrain = TerrainType.WATER

        // column 12
        grid[11][0].terrain = TerrainType.WATER
        grid[11][1].terrain = TerrainType.WATER
        grid[11][2].terrain = TerrainType.WATER
        grid[11][3].terrain = TerrainType.FOREST
        grid[11][4].terrain = TerrainType.ROAD
        grid[11][5].terrain = TerrainType.MOUNTAIN
        grid[11][6].terrain = TerrainType.WATER
        grid[11][7].terrain = TerrainType.WATER
        grid[11][8].terrain = TerrainType.WATER

        // column 13
        grid[12][0].terrain = TerrainType.WATER
        grid[12][1].terrain = TerrainType.MOUNTAIN
        grid[12][2].terrain = TerrainType.HOUSE
        grid[12][3].terrain = TerrainType.GRASS
        grid[12][4].terrain = TerrainType.ROAD
        grid[12][5].terrain = TerrainType.GRASS
        grid[12][6].terrain = TerrainType.HOUSE
        grid[12][7].terrain = TerrainType.GRASS
        grid[12][8].terrain = TerrainType.WATER

        // column 14
        grid[13][0].terrain = TerrainType.WATER
        grid[13][1].terrain = TerrainType.FOREST
        grid[13][2].terrain = TerrainType.GRASS
        grid[13][3].terrain = TerrainType.FOREST
        grid[13][4].terrain = TerrainType.MONUMENT
        grid[13][4].buildingOwner = OwnerTyp.BLUE
        grid[13][5].terrain = TerrainType.HOUSE
        grid[13][6].terrain = TerrainType.MOUNTAIN
        grid[13][7].terrain = TerrainType.MOUNTAIN
        grid[13][8].terrain = TerrainType.WATER
    }

}
