package pam.project.chesstournamentmanager.database

import android.arch.persistence.room.*
import pam.project.chesstournamentmanager.model.Player

@Dao
interface PlayersDao {

    @Query("SELECT * FROM Players ORDER BY surname, name")
    fun getAllPlayers() : List<Player>

    @Insert
    fun insertPlayer(player : Player)

    @Delete
    fun removePlayer(players : List<Player>)

    @Update
    fun updatePlayer(player : Player)
}