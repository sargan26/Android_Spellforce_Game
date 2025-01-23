package com.mitterlehner.spellforce

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mitterlehner.spellforce.game.GameState
import com.mitterlehner.spellforce.game.OwnerTyp
import com.mitterlehner.spellforce.game.Player
import com.mitterlehner.spellforce.game.Swordsman
import com.mitterlehner.spellforce.game.Unit
import com.mitterlehner.spellforce.ui.GameView

class MainActivity : AppCompatActivity(), GameView.GameViewCallback {
    private var gameState = GameState.PLAYER_TURN
    val player = Player()
    private var roundNumber = 1;
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameView = findViewById(R.id.gameView)

        val gameView = findViewById<GameView>(R.id.gameView)
        gameView.callback = this

        val endTurnButton: Button = findViewById(R.id.endTurnButton)

        // End player turn when button pressed
        endTurnButton.setOnClickListener {
            if (gameState == GameState.PLAYER_TURN) {
                endPlayerTurn()
            }
        }

        startGameLoop()
    }

    private fun startGameLoop() {
        when (gameState) {
            GameState.PLAYER_TURN -> {
                findViewById<TextView>(R.id.goldAmount).apply {
                    text = "Gold: " + player.updateGold().toString()
                }

            }
            GameState.ENEMY_TURN -> {
                gameView.handleEnemyTurn()

                println("Gegnerzug beendet.")
                gameState = GameState.PLAYER_TURN
                startGameLoop() // Next player turn
            }
            GameState.GAME_OVER -> {
                println("Spiel beendet.")
            }
        }
    }

    private fun endPlayerTurn() {
        findViewById<TextView>(R.id.incomeAmount).apply {
            text = "Einkommen: +" + player.updateIncome().toString()
        }
        roundNumber += 1
        findViewById<TextView>(R.id.roundNumber).apply {
            text = "Runde: " + roundNumber.toString()
        }

        if (roundNumber % 2 == 0) {
            gameView.spawnEnemyUnit()
        }

        // Reset hasMoved, hasAttacked at the end of the turn
        val board = gameView.board
        for (row in board.grid) {
            for (cell in row) {
                if (cell.unit != null) {
                    cell.unit!!.hasMoved = false
                    cell.unit!!.hasAttacked = false
                }
            }
        }
        gameView.highlightedCells.clear()
        gameView.attackRangeCells.clear()

        gameState = GameState.ENEMY_TURN
        startGameLoop()
    }

    override fun updateGoldAmount(gold: Int) {
        findViewById<TextView>(R.id.goldAmount).apply {
            text = "Gold: $gold"
        }
    }

    override fun endGame() {
        gameState == GameState.GAME_OVER
    }

    override fun updateIncome(income: Int) {
        findViewById<TextView>(R.id.incomeAmount).apply {
            text = "Einkommen: +" + player.updateIncome().toString()
        }
    }

    override fun onUnitSelected(unit: Unit?) {
        updateUnitStatus(unit)
    }

    fun updateUnitStatus(unit: Unit?) {
        findViewById<TextView>(R.id.unitName).text = "Name: ${unit?.name ?: "None"}"
        findViewById<TextView>(R.id.unitOwner).text = "Owner: ${unit?.owner ?: "None"}"
        findViewById<TextView>(R.id.unitHealth).text = "Health: ${unit?.currentHealth ?: 0} / ${unit?.maxHealth ?: 0}"
        findViewById<TextView>(R.id.unitAttack).text = "Attack: ${unit?.attack ?: 0}"
        findViewById<TextView>(R.id.unitMovement).text = "Movement Range: ${unit?.movementRange ?: 0}"
        findViewById<TextView>(R.id.unitHasMoved).text = "Has Moved: ${unit?.hasMoved ?: false}"
    }
}