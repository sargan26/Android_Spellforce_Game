package com.mitterlehner.spellforce.game

class Player {
    var gold: Int = 300 // initial gold
    var basicIncome: Int = 150 // basic income
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
}