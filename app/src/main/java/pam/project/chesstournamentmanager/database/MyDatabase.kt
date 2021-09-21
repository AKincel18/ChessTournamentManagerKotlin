package pam.project.chesstournamentmanager.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import pam.project.chesstournamentmanager.model.Player
import pam.project.chesstournamentmanager.staticdata.Constants

@Database(entities = [Player::class], version = 1, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {

    companion object {
        var instance: MyDatabase? = null

        fun getInstance(context : Context) : MyDatabase? {
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext,
                        MyDatabase::class.java, Constants.DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()

            }
            return instance
        }
    }

    abstract fun playersDao() : PlayersDao
}