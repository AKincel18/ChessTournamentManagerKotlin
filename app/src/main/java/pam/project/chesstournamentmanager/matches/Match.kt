package pam.project.chesstournamentmanager.matches

import pam.project.chesstournamentmanager.model.TournamentPlayer
import java.io.Serializable

class Match(var round: Int, var player1: TournamentPlayer?, var player2: TournamentPlayer?) : Serializable {

    var matchResult : MatchResult? = null

}