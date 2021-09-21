package pam.project.chesstournamentmanager.staticdata.adapters

import android.content.Context
import android.widget.ArrayAdapter

class SpinnerAdapter(context: Context, resource: Int, objects: MutableList<String>) :
    ArrayAdapter<String>(context, resource, objects) {

    override fun getCount(): Int {
        val count : Int = super.getCount()
        return if (count > 0) count - 1 else count
    }
}