package com.mitterlehner.spellforce.game

class GridCell(var row: Int,
               var col: Int,
               var terrain: TerrainType,
               var buildingOwner: OwnerTyp,
               var unit: Unit?
) {
}
