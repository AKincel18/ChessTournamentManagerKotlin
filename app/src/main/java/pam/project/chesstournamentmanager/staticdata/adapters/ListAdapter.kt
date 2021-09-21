package pam.project.chesstournamentmanager.staticdata.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import pam.project.chesstournamentmanager.R
import pam.project.chesstournamentmanager.staticdata.Constants

class ListAdapter(context: Context, resource: Int, textViewResourceId: Int, objects: MutableList<String>) :
    ArrayAdapter<String>(context, resource, textViewResourceId, objects) {

    private var rLayout : Int = resource

    private var itemList : List<String>? = objects

    private var context2: Context? = context


    private companion object {
        class ViewHolder {
            var noColumn : TextView? = null

            var nameColumn : TextView? = null

            var internationalRankColumn : TextView? = null

            var polishRankColumn : TextView? = null

        }
    }

    override fun getView(position: Int, convertView : View?, parent : ViewGroup) : View? {
        var rowView : View? = convertView

        if (rowView == null) {

            val inflater = context2!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowView = inflater.inflate(rLayout, parent, false)

            val holder = ViewHolder()
            holder.noColumn = rowView.findViewById(R.id.lp_row_layout)
            holder.nameColumn = rowView.findViewById(R.id.name_player_row_layout)
            holder.internationalRankColumn = rowView.findViewById(R.id.international_ranking_row_layout)
            holder.polishRankColumn = rowView.findViewById(R.id.polish_ranking_row_layout)
            rowView.tag = holder
        }
        val items = itemList!![position].split(Constants.COMMA)
        val holder = rowView!!.tag as ViewHolder

        holder.noColumn!!.text = items[0]
        holder.nameColumn!!.text = items[1]
        holder.internationalRankColumn!!.text = items[2]
        holder.polishRankColumn!!.text = items[3]

        return rowView

    }
}