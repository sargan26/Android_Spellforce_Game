package com.mitterlehner.spellforce.game

data class Unit(val name: String,
                val maxHealth: Int,
                val currentHealth: Int,
                val attack: Int,
                val movementRange: Int) {

}
