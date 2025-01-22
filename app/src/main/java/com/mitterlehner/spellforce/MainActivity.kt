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
import com.mitterlehner.spellforce.game.Player
import com.mitterlehner.spellforce.ui.GameView

class MainActivity : AppCompatActivity(), GameView.GameViewCallback {
    private var gameState = GameState.PLAYER_TURN
    public val player = Player()
    private var roundNumber = 1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Verweisen Sie auf das vorhandene GameView in der XML
        val gameView = findViewById<GameView>(R.id.gameView)
        gameView.callback = this // Setzen Sie den Callback

        val endTurnButton: Button = findViewById(R.id.endTurnButton)

        // Spielerphase beenden, wenn Button gedrückt wird
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


                // Update UI mit dem Einkommen
                println("Spieler erhält  Gold.")

                // Spieleraktionen hier verwalten...



            }
            GameState.ENEMY_TURN -> {
                // Gegnerzug automatisieren...
                println("Gegnerzug beendet.")

                gameState = GameState.PLAYER_TURN
                startGameLoop() // Nächste Spielerphase
            }
            else -> println("Spiel beendet.")
        }
    }

    private fun endPlayerTurn() {
        println("Spieler beendet seine Runde.")
        findViewById<TextView>(R.id.incomeAmount).apply {
            text = "Einkommen: +" + player.updateIncome().toString()
        }
        roundNumber += 1
        findViewById<TextView>(R.id.roundNumber).apply {
            text = "Runde: " + roundNumber.toString()
        }

        gameState = GameState.ENEMY_TURN
        startGameLoop() // Gegnerphase starten
    }

    override fun updateGoldAmount(gold: Int) {
        Log.d("GameDebug", "updateGoldAmount(${gold})")
        findViewById<TextView>(R.id.goldAmount).apply {
            text = "Gold: $gold"
        }
    }
}