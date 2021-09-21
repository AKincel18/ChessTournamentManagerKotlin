package pam.project.chesstournamentmanager.model

import android.arch.persistence.room.*
import pam.project.chesstournamentmanager.database.DateConverter
import java.io.Serializable
import java.util.*

@Entity(tableName = "players")
open class Player : Serializable{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id : Long = 0

    @ColumnInfo(name = "name")
    var name : String = ""

    @ColumnInfo(name = "surname")
    var surname : String = ""

    @ColumnInfo(name = "polish_ranking")
    var polishRanking : Int = 0

    @ColumnInfo(name = "international_ranking")
    var internationalRanking : Int = 0

    @ColumnInfo(name = "date_of_birth")
    @TypeConverters(DateConverter::class)
    var dateOfBirth : Date = Date()

    @Ignore
    constructor()

    @Ignore
    constructor(name: String, surname: String, polishRanking: Int, internationalRanking: Int, dateOfBirth: Date) {
        this.name = name
        this.surname = surname
        this.polishRanking = polishRanking
        this.internationalRanking = internationalRanking
        this.dateOfBirth = dateOfBirth
    }

    @Ignore
    constructor(player : Player) {
        this.name = player.name
        this.surname = player.surname
        this.polishRanking = player.polishRanking
        this.internationalRanking = player.internationalRanking
        this.dateOfBirth = player.dateOfBirth
    }

    constructor(name: String, surname: String, dateOfBirth: Date) {
        this.name = name
        this.surname = surname
        this.dateOfBirth = dateOfBirth
    }

    override fun toString(): String {
        return "$surname  $name"
    }


}