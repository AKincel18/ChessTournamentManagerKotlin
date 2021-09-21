package pam.project.chesstournamentmanager.systems

import pam.project.chesstournamentmanager.matches.Match
import pam.project.chesstournamentmanager.model.Player
import pam.project.chesstournamentmanager.model.TournamentPlayer
import pam.project.chesstournamentmanager.staticdata.Constants
import java.util.*
import kotlin.collections.ArrayList
import pam.project.chesstournamentmanager.matches.MatchResult

class Knockout(var roundsNumber: Int, tournamentPlayer: ArrayList<Player>?) {

    companion object {

        private var INSTANCE : Knockout? = null

        fun initKnockout(roundsNumber : Int, players : ArrayList<Player>) : Knockout {
            if (INSTANCE == null) {
                INSTANCE = Knockout(roundsNumber, players)
            }
            return INSTANCE as Knockout
        }

        fun resetTournament() {
            INSTANCE = null
        }

        fun getINSTANCE() : Knockout?{
            return INSTANCE
        }
    }

    var currentRound : Int = 1

    var isFinishedTournament : Boolean = false

    private var tournamentPlayer : ArrayList<TournamentPlayer>? = ArrayList<TournamentPlayer>()

    var matches = ArrayList<List<Match>>() //rows -> number of rounds, columns -> matches

    private var matchesTmp = ArrayList<Match>()

    private var draw : ArrayList<TournamentPlayer>? = null

    init {
        for (p : Player in tournamentPlayer!!) {
            this.tournamentPlayer!!.add(TournamentPlayer(p))
        }
    }


    fun initDraw() {

        draw = ArrayList<TournamentPlayer>(Arrays.asList<TournamentPlayer>(*arrayOfNulls<TournamentPlayer>(Math.pow(
                        2.0,
                        roundsNumber.toDouble()
                    ).toInt())))

        for (i in tournamentPlayer!!.size until draw!!.size) {
            tournamentPlayer!!.add(i, TournamentPlayer(Constants.BYE))
        }

        val matchList = ArrayList<Match>()
        for (i in 0 until draw!!.size) {
            matchList.add(
                Match(currentRound, tournamentPlayer!![i], tournamentPlayer!![tournamentPlayer!!.size - 1 - i]
            )
            )
        }

        var upperHalf = 0
        var bottomHalf = tournamentPlayer!!.size - 1

        for ((count, match) in matchList.withIndex()) {
            if (count % 2 == 0) {
                draw!![upperHalf] = match.player1!!
                upperHalf++
                draw!![upperHalf] = match.player2!!
                upperHalf++

            } else {
                draw!![bottomHalf] = match.player1!!
                bottomHalf--
                draw!![bottomHalf] = match.player2!!
                bottomHalf--
            }
        }

        addMatch()

    }

    private fun addMatch(){

        for ((pos, i) in (0 until draw!!.size step 2).withIndex()){
            matchesTmp.add(pos, Match(currentRound, draw!![i], draw!![i + 1]))
        }

        matches.add(matchesTmp)
        matchesTmp = ArrayList()
    }

    fun setResult(result : ArrayList<MatchResult>){
        for (i in 0 until result.size) {
            if (result[i] == MatchResult.WHITE_WON) {
                matches[currentRound - 1][i].matchResult = MatchResult.WHITE_WON
                draw!!.remove(draw!![i + 1])
            }
            else {
                matches[currentRound - 1][i].matchResult = MatchResult.BLACK_WON
                draw!!.remove(draw!![i])
            }

        }

        if (currentRound != roundsNumber) {
            currentRound++
            addMatch()
        } else {
            isFinishedTournament = true
        }
    }

    fun sortPlayers() : ArrayList<TournamentPlayer> {
        val results = ArrayList<TournamentPlayer>()

        //add winner on first position
        val matchFinal = matches[matches.size - 1][0]

        if (matchFinal.matchResult == MatchResult.WHITE_WON) {
            results.add(matchFinal.player1!!)
        }
        else {
            results.add(matchFinal.player2!!)
        }

        //add others
        for (i in matches.size - 1 downTo 0){
            for (match in matches[i]) {
                if (match.matchResult == MatchResult.WHITE_WON) {
                    results.add(match.player2!!)
                }
                else {
                    results.add(match.player1!!)
                }
            }
        }
        return results

    }
}