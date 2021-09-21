package pam.project.chesstournamentmanager.database

import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.TypeConverters
import java.sql.Timestamp
import java.util.*

class DateConverter {

        @TypeConverter
        fun toDate(timestamp: Long?): Date? {
            return if (timestamp == null) null else Date(timestamp)
        }

        @TypeConverter
        fun toTimestamp(date: Date?): Long? {
            return date?.time
        }

}