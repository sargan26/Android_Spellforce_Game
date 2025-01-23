package com.mitterlehner.spellforce.game

class Player {
    var gold: Int = 300 // initial gold
        get() = field
        set(value) {
            field = value
        }

    var basicIncome: Int = 150 // initial income
    var currentIncome: Int = 150
    var houseCount: Int = 0
    var houseIncome: Int = 50 // additional income per house

    fun updateIncome(): Int {
        currentIncome = basicIncome + (houseCount * houseIncome)
        return currentIncome
    }

    fun updateGold(): Int {
        gold += currentIncome
        return gold
    }

    fun reset() {
        gold = 300
        houseCount = 0
        currentIncome = 150
    }
}