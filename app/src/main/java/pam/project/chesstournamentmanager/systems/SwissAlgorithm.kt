package pam.project.chesstournamentmanager.systems

import pam.project.chesstournamentmanager.matches.Match
import pam.project.chesstournamentmanager.matches.MatchResult
import pam.project.chesstournamentmanager.model.Colors
import pam.project.chesstournamentmanager.model.Player
import pam.project.chesstournamentmanager.model.TournamentPlayer
import pam.project.chesstournamentmanager.staticdata.Constants
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class SwissAlgorithm(
    var roundsNumber: Int,// true - buchholz, false - median buchholz
    var placeOrder: Boolean
) : Serializable{

    companion object {
      private var INSTANCE : SwissAlgorithm? = null

      fun initSwissAlgorithm(roundsNumber : Int, placeOrder : Boolean) : SwissAlgorithm {
          if (INSTANCE == null) {
              INSTANCE = SwissAlgorithm(roundsNumber, placeOrder)
          }
          return INSTANCE as SwissAlgorithm
      }
        fun resetTournament() {
            INSTANCE = null
        }

        fun getINSTANCE() : SwissAlgorithm? {
            return INSTANCE
        }
    }

    var currentRound = 1

    private var playersNumber: Int = 0

    var tournamentPlayers: ArrayList<TournamentPlayer>? = null


    var matches: ArrayList<List<Match>> = ArrayList<List<Match>>() //rows -> number of rounds, columns -> matches

    private var matchesTmp: ArrayList<Match> = ArrayList<Match>()

    var finishedTournament = false

    var even: Boolean = false

    private var groupsPlayers: ArrayList<ArrayList<TournamentPlayer>> = ArrayList<ArrayList<TournamentPlayer>>() //rows -> number of group, columns -> position in a group

    fun initTournamentPlayers(players : ArrayList<Player>) {
        tournamentPlayers = ArrayList()

        for (player in players) {
            tournamentPlayers!!.add(TournamentPlayer(player))
        }

        playersNumber = tournamentPlayers!!.size
        even = playersNumber % 2 == 0
    }

    fun drawFirstRound(){
        for (i in 0 until playersNumber / 2) {
            val player1 = tournamentPlayers!![i]
            val player2 = tournamentPlayers!![playersNumber / 2 + i]
            addFirstMatchRound(player1, player2)
        }

        if (playersNumber % 2 != 0){
            val player = tournamentPlayers!![playersNumber - 1]
            addMatchVersusBye(player)
        }

        matches.add(matchesTmp)
        matchesTmp = ArrayList()
    }

    private fun reset() {
        for (player in tournamentPlayers!!) {
            player.upper = false
        }
    }

    fun drawNextRound() {
        sortPlayerDuringTournament()
        groupsPlayers = prepareGroups()

        reset()
        var byePlayer : TournamentPlayer? = null
        if (!even) {
            byePlayer = findByePlayer()
        }

        var groupIterator = groupsPlayers.listIterator()
        var groupNumber = 0

        while (groupNumber < groupsPlayers.size) {
            var removeEmptyGroup = false
            var backToHigerGroup = false
            val playerIterator = groupsPlayers[groupNumber].listIterator()

            while (playerIterator.hasNext()) {
                val player = playerIterator.next()
                if (!player.hasOpponent(currentRound)) {
                    if (!findOpponent(player, groupsPlayers[groupNumber].size, groupNumber)) { //not found opponent in his points group

                        if (groupNumber == 0) {
                            player.upper = false
                        }

                        //go to higher group
                        if (player.upper || groupNumber == groupsPlayers.size - 1) {

                            groupNumber--
                            groupsPlayers[groupNumber].add(0, player) //add player to higher group, on the first position

                            playerIterator.remove() //remove player from current group
                            removeMatchesInGroup(groupNumber)  //remove all matches from higher group
                            backToHigerGroup = true  //set flag
                            break
                        }
                        else {
                            //go to lower group
                            groupsPlayers[groupNumber + 1].add(0, player)

                            if (groupNumber + 1 == groupsPlayers.size - 1) {
                                player.upper = true
                            }
                            removeMatchesInGroup(groupNumber + 1)
                            playerIterator.remove()

                        }

                        //remove empty group
                        if (groupsPlayers[groupNumber].isEmpty()) {
                            var list = groupIterator.next()
                            groupIterator.remove()
                            removeEmptyGroup = true
                        }
                    }
                }
            }

            if (removeEmptyGroup || backToHigerGroup) {
                groupIterator = groupsPlayers.listIterator(groupNumber)
            }
            else {
                groupNumber++
                groupIterator.next()
            }
        }

        if (!even) {
            addMatchVersusBye(byePlayer!!) //add the last one match
        }

        matches.add(matchesTmp)
        matchesTmp = ArrayList()
    }

    private fun removeMatchesInGroup(groupNumber: Int) {
        val lastGroup = groupsPlayers[groupNumber]
        val matchIterator = matchesTmp.listIterator(matchesTmp.size)

        while (matchIterator.hasPrevious()) {
            val match = matchIterator.previous()

            for (player in lastGroup) {
                if (match.player1 == player || match.player2 == player) {
                    removeMatchFromPlayers(match.player1, match.player2)
                    matchIterator.remove()
                    break
                }
            }
        }
    }

    private fun removeMatchFromPlayers(player1: TournamentPlayer?, player2: TournamentPlayer?) {
        player1!!.removeLastMatch()
        player2!!.removeLastMatch()
    }

    private fun findOpponent(player: TournamentPlayer, playersInGroup: Int, groupNumber: Int): Boolean {

        //find opponent in the second subgroup
        for (i in playersInGroup / 2 until playersInGroup) {
            val player2 = groupsPlayers[groupNumber][i]

            if (!player2.hasOpponent(currentRound) && !player.isPlayedTogether(player2) && (player !== player2)){
                addMatch(player, player2)
                return true
            }
        }

        //find opponent in the first subgroup
        for (i in 0 until playersInGroup / 2) {
            val player2 = groupsPlayers[groupNumber][i]

            if (!player2.hasOpponent(currentRound) && !player.isPlayedTogether(player2) && player !== player2) {
                addMatch(player, player2)
                return true
            }
        }
        return false
    }

    private fun addMatch(player1: TournamentPlayer, player2: TournamentPlayer) {
        if (selectColors(player1, player2)) {
            matchesTmp.add(Match(currentRound, player1, player2))
            player1.setPrevColor(Colors.WHITE)
            player2.setPrevColor(Colors.BLACK)
        }
        else {
            matchesTmp.add(Match(currentRound, player2, player1))
            player1.setPrevColor(Colors.BLACK)
            player2.setPrevColor(Colors.WHITE)
        }
        player1.setPrevOpponent(player2)
        player2.setPrevOpponent(player1)
    }

    //return true if player1 has white color, false = black
    private fun selectColors(player1: TournamentPlayer, player2: TournamentPlayer): Boolean {
        return if (player1.countColorInTheRow() > player2.countColorInTheRow())
            (player1.getLastColor().opposite() == Colors.WHITE)
        else
            player2.getLastColor().opposite() != Colors.BLACK

    }

    private fun findByePlayer(): TournamentPlayer? {
        var i = playersNumber - 1
        while (i >= 0) {
            val player = tournamentPlayers!![i]

            if (!player.bye) { //no bye before
                removePlayerFromDrawing(player)
                removeEmptyGroup()
                return player
            }
            i--
        }
        return null
    }

    private fun removeEmptyGroup() {
        val it = groupsPlayers.listIterator(groupsPlayers.size)
        while ((it as ListIterator<List<TournamentPlayer>>).hasPrevious()) {
            val players = (it as ListIterator<ArrayList<TournamentPlayer>>).previous()

            if (players.isEmpty()) {
                it.remove()
                break
            }
        }
    }

    private fun removePlayerFromDrawing(playerToRemove: TournamentPlayer) {
        for (list in groupsPlayers) {
            for (player in list) {
                if (player === playerToRemove) {
                    list.remove(player)
                    break
                }
            }
        }
    }

    private fun prepareGroups(): ArrayList<ArrayList<TournamentPlayer>> {
        var pointsTemp = tournamentPlayers!![0].points
        groupsPlayers.clear()

        var list = ArrayList<TournamentPlayer>()

        for ((currentPos, player) in tournamentPlayers!!.withIndex()) {
            var added = true
            while (player.points != pointsTemp) {
                pointsTemp-=0.5f

                if (added) {
                    groupsPlayers.add(list)
                    list = ArrayList()
                    added = false
                }
            }
            list.add(player)
            if (currentPos == tournamentPlayers!!.size - 1)
                groupsPlayers.add(list)

        }
        return groupsPlayers
    }

    private fun sortPlayerDuringTournament() {
        tournamentPlayers!!.sortWith(Comparator { o1, o2 ->
            var c = java.lang.Float.compare(o2.points, o1.points) //descending order
            if (c == 0) {
                c = Integer.compare(o2.internationalRanking, o1.internationalRanking)
                if (c == 0) {
                    c = Integer.compare(o2.polishRanking, o1.polishRanking)
                    if (c == 0) {
                        c = o1.toString().compareTo(o2.toString())
                    }
                }
            }
            c
        })
    }

    private fun sortPlayerAfterTournament() {
        tournamentPlayers!!.sortWith(Comparator { o1, o2 ->
            var c = java.lang.Float.compare(o2.points, o1.points) //descending order

            if (c == 0) {
                if (placeOrder)
                    c = java.lang.Float.compare(o2.buchholzPoints, o1.buchholzPoints)
                else
                    c = java.lang.Float.compare(o2.medianBuchholzMethod, o1.medianBuchholzMethod)
            }
            c
        })
    }


    private fun addFirstMatchRound(player1: TournamentPlayer, player2: TournamentPlayer) {
        val random = Random()
        if (random.nextBoolean()) {//player1 = white
            matchesTmp.add(Match(currentRound, player1, player2))
            player1.setPrevColor(Colors.WHITE)
            player2.setPrevColor(Colors.BLACK)
        } else { //player2 = black
            matchesTmp.add(Match(currentRound, player2, player1))
            player2.setPrevColor(Colors.WHITE)
            player1.setPrevColor(Colors.BLACK)
        }

        player1.setPrevOpponent(player2)
        player2.setPrevOpponent(player1)
    }

    private fun addMatchVersusBye(player: TournamentPlayer) {

        val bye = TournamentPlayer()
        bye.name = Constants.BYE
        bye.surname = Constants.EMPTY
        matchesTmp.add(Match(currentRound, player, bye))
        player.bye = true
        player.setPrevOpponent(bye)
        player.setPrevColor(Colors.NO_COLOR)
    }

    private fun buchholzMethod() {
        for (player in tournamentPlayers!!) {
            var points = 0.0f

            for (opponent in player.prevOpponents) {
                points += opponent.points
            }
            if (player.bye) {
                points -= 0.5f
            }
            player.buchholzPoints = points
        }
    }

    private fun medianBuchholzMethod() {
        for (player in tournamentPlayers!!) {
            val opponents = getOrderOpponent(player.prevOpponents)
            var points = 0.0f
            for (i in 1 until opponents.size - 1) {
                points += opponents[i].points
            }
            player.medianBuchholzMethod = points
        }
    }

    private fun getOrderOpponent(playerList: ArrayList<TournamentPlayer>): ArrayList<TournamentPlayer> {
        playerList.sortWith(Comparator { o1, o2 -> java.lang.Float.compare(o1.points, o2.points) })
        return playerList
    }

    fun setResult(results : ArrayList<MatchResult> ) {
        val matchedInGroup = matches[currentRound - 1]

        for ((i, matchResult) in results.withIndex()) {
            matchedInGroup[i].matchResult = matchResult
            val player1 = matchedInGroup[i].player1
            val player2 = matchedInGroup[i].player2

            when (matchResult) {
                MatchResult.WHITE_WON  -> player1!!.addPoints(1.0f) //player1 won
                MatchResult.BLACK_WON  -> player2!!.addPoints(1.0f) //player2 won
                MatchResult.DRAW -> { //draw
                    player1!!.addPoints(0.5f)
                    player2!!.addPoints(0.5f)
                }
            }
        }

        if (currentRound == roundsNumber) {
            finishedTournament = true


            if (placeOrder) {
                buchholzMethod()
            } else {
                medianBuchholzMethod()

            }

            sortPlayerAfterTournament()
        } else {
            currentRound++
        }
    }

}
