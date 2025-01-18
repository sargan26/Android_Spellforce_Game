package com.mitterlehner.spellforce.game

import androidx.annotation.IntegerRes

class GridCell(var row: Int,
               var col: Int,
               var terrain: TerrainType,
               var isOccupied: Boolean,
               var buildingOwner: OwnerTyp,
               var unit: UnitType,
               var unitOwner: OwnerTyp
) {
}
