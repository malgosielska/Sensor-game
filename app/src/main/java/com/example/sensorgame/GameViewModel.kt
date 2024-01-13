package com.example.sensorgame

import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {

    var counter = INITIAL_LIVES

    var isBallMoving = true

    fun decrementCounter() {
        counter--
    }

    fun resetGame() {
        counter = INITIAL_LIVES
        isBallMoving = true
    }

    companion object {
        const val INITIAL_LIVES = 5
    }
}
