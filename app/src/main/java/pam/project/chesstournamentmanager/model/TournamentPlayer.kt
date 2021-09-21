package pam.project.chesstournamentmanager.model

import pam.project.chesstournamentmanager.staticdata.Constants

class TournamentPlayer : Player {

    var points : Float = 0.0F

    var prevOpponents = java.util.ArrayList<TournamentPlayer>()

    private var prevColors : ArrayList<Colors> = ArrayList<Colors>()

    var buchholzPoints : Float = 0.0F

    var medianBuchholzMethod : Float = 0.0F

    var bye : Boolean = false

    var upper : Boolean = false

    constructor()

    constructor(player : Player)  : super (player)

    constructor(bye : String) {
        this.surname = bye
        this.name = Constants.EMPTY
    }

    fun removeLastMatch() {
        prevOpponents.removeAt(prevOpponents.size - 1)
        prevColors.removeAt(prevColors.size - 1)
    }

    fun isPlayedTogether(player : TournamentPlayer) : Boolean {
        for (tournamentPlayer in prevOpponents) {
            if (tournamentPlayer === player)
                return true
        }
        return false

    }

    fun hasOpponent(currentRound : Int) : Boolean {
        return currentRound == prevOpponents.size
    }

    fun countColorInTheRow() : Int {
        if (prevColors.size == 1) {
            return 1;
        }
        var counter = 1

        for (i in prevColors.size - 1 downTo 1) {
            if (prevColors[i] != Colors.NO_COLOR && prevColors[i] == prevColors[i - 1]) {
                counter++
            } else
                return counter
        }

        return counter
    }

    fun setPrevColor(color : Colors){
        this.prevColors.add(color)
    }

    fun setPrevOpponent(player : TournamentPlayer){
        this.prevOpponents.add(player)
    }

    fun getLastColor() : Colors {
        return prevColors[prevColors.size - 1]
    }

    fun addPoints(points : Float) {
        this.points += points
    }
}

