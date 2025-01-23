package com.mitterlehner.spellforce.game

open class Unit(val name: String,
                var owner: OwnerTyp,
                val maxHealth: Int,
                var currentHealth: Int,
                val attack: Int,
                val movementRange: Int,
                val attackRange: Int,
                var hasMoved: Boolean = false,
                var hasAttacked: Boolean = false) {
}
