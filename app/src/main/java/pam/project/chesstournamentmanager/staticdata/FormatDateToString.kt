package pam.project.chesstournamentmanager.staticdata

import pam.project.chesstournamentmanager.staticdata.Constants
import java.util.*

class FormatDateToString {

    companion object {
        fun parseDateFromDatabase(date : Date) : String {
            val calendar = Calendar.getInstance()
            calendar.time = date
            return parseDateFromDatePicker((calendar.get(Calendar.YEAR)),
                (calendar.get(Calendar.MONTH)),
                calendar.get(Calendar.DAY_OF_MONTH))
        }

        fun parseDateFromDatePicker(year : Int, month: Int, day : Int) : String {
            var monthTmp = month
            monthTmp++
            val monthString = if (monthTmp >= 10) monthTmp.toString() else Constants.ZERO + monthTmp
            val dayString = if (day >= 10) day.toString() else Constants.ZERO + day
            return dayString + Constants.DASH + monthString + Constants.DASH + year
        }

    }
}