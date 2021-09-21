package pam.project.chesstournamentmanager.model

enum class Colors {
    WHITE, BLACK, NO_COLOR;

    fun opposite() : Colors? {
        return when (this) {
            WHITE -> BLACK
            BLACK -> WHITE
            NO_COLOR -> null
        }
    }
}