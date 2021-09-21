package pam.project.chesstournamentmanager.activities

import android.content.Intent
import android.content.res.Resources
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import pam.project.chesstournamentmanager.R
import pam.project.chesstournamentmanager.model.Player
import pam.project.chesstournamentmanager.staticdata.adapters.ListAdapter
import pam.project.chesstournamentmanager.staticdata.dialogbox.GeneralDialogFragment
import pam.project.chesstournamentmanager.staticdata.dialogbox.OnDialogFragmentClickListener
import pam.project.chesstournamentmanager.systems.Knockout
import pam.project.chesstournamentmanager.systems.SwissAlgorithm
import java.lang.Integer.compare
import java.lang.NumberFormatException
import java.util.*

class ConfigureTournament : AppCompatActivity(), OnDialogFragmentClickListener {


    private var players: ArrayList<Player>? = null

    private var listView: ListView? = null

    private var switch1: Switch? = null

    private var roundsTextView: TextView? = null

    private var placeOrder = true // true - buchholz, false - median buchholz

    private var editText: EditText? = null

    private var chosenSystem = true //true - swiss, false - knockout

    private var optimalCountOfRounds: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure_tournament)

        val countOfPlayersTextView = findViewById<TextView>(R.id.count_of_players_text_view)
        val i = intent
        players = i.getSerializableExtra(getString(R.string.players)) as ArrayList<Player>
        countOfPlayersTextView.text = players!!.size.toString()
        listView = findViewById(R.id.players_list_view)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        switch1 = findViewById(R.id.count_of_number_switch)
        roundsTextView = findViewById(R.id.auto_count_of_rounds)

        initSpinnerPlaceOrder()
        comparatorByStandardOrder()

        initSpinnerSystems()

        optimalCountOfRounds = Math.ceil(Math.log(players!!.size.toDouble()) / Math.log(2.0)).toInt() // ceil(log2(player's number)),  log2 = (Math.log(x) / Math.log(2));
        choiceRoundsSwitchImplementation()
        startTournament()
    }

    private fun initSpinnerSystems() {
        val spinner = findViewById<Spinner>(R.id.system_spinner)
        val adapter = ArrayAdapter.createFromResource(this, R.array.systems, R.layout.color_spinner_config_layout)
        adapter.setDropDownViewResource(R.layout.spinner_config_tournament)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (parent.selectedItemPosition == 0) {
                    chosenSystem = true
                    buildSwissView()
                } else {
                    chosenSystem = false
                    buildKnockoutView()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    private fun initSpinnerPlaceOrder() {
        val spinner = findViewById<Spinner>(R.id.place_order_spinner)
        val adapter = ArrayAdapter.createFromResource(this, R.array.place_order, R.layout.color_spinner_config_layout)
        adapter.setDropDownViewResource(R.layout.spinner_config_tournament)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                placeOrder = parent.selectedItemPosition == 0
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    private fun buildSwissView() {
        val linearLayout = findViewById<LinearLayout>(R.id.layout_set_round)
        linearLayout.removeAllViews()

        linearLayout.addView(switch1)
        linearLayout.addView(roundsTextView)

        val ll = findViewById<LinearLayout>(R.id.methods_layout)
        ll.visibility = View.VISIBLE
    }

    private fun buildKnockoutView() {
        val layout = findViewById<LinearLayout>(R.id.layout_set_round)
        layout.removeAllViews()

        val textView = TextView(this)

        val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        textView.layoutParams = params
        textView.gravity = Gravity.START or Gravity.CENTER
        textView.text = getString(R.string.number_of_rounds)
        textView.setTextColor(getColor(R.color.colorAccent))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        layout.addView(textView)


        val textView2 = TextView(this)
        textView2.layoutParams = params
        textView2.gravity = Gravity.START or Gravity.CENTER
        textView2.text = optimalCountOfRounds.toString()
        textView2.setTextColor(getColor(R.color.colorPrimaryDark))
        textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
        textView2.setPaddingRelative(5, 0, 0, 0)
        layout.addView(textView2)

        val ll = findViewById<LinearLayout>(R.id.methods_layout)
        ll.visibility = View.INVISIBLE
    }



    private fun startTournament() {
        val startTournament : Button = findViewById(R.id.start_tournament_button)
        startTournament.setOnClickListener {
            val i = Intent(applicationContext, Tournament::class.java)
            var startActivity = false

            if (chosenSystem) {
                if (switch1!!.isChecked) {
                    try {
                        val roundNumber = editText!!.text.toString().toInt()
                        if (maxNumberCount() >= roundNumber && roundNumber != 0) {
                            startActivity = true
                            i.putExtra(getString(R.string.rounds_number), roundNumber)
                        }
                        else {
                            val dialog = GeneralDialogFragment.newInstance(
                                getString(R.string.wrong_rounds_title),
                                getString(R.string.wrong_rounds_message, players!!.size, maxNumberCount(), roundNumber),
                                getString(R.string.exit_button)
                            )
                            dialog.show(supportFragmentManager, getString(R.string.wrong_rounds_title))
                        }
                    } catch (e : NumberFormatException) {
                        val dialog = GeneralDialogFragment.newInstance(
                            getString(R.string.title_error),
                            getString(R.string.empty_rounds_number),
                            getString(R.string.exit_button)
                        )
                        dialog.show(supportFragmentManager, getString(R.string.title_error))
                    }
                } else {
                    startActivity = true
                    i.putExtra(getString(R.string.rounds_number), Integer.valueOf(optimalCountOfRounds))
                }
            } else {
                startActivity = true
                i.putExtra(getString(R.string.rounds_number), Integer.valueOf(optimalCountOfRounds))
            }

            if (startActivity) {
                i.putExtra(getString(R.string.players), players)
                i.putExtra(getString(R.string.chosen_system), chosenSystem)
                i.putExtra(getString(R.string.place_order), placeOrder)
                SwissAlgorithm.resetTournament()
                Knockout.resetTournament()
                startActivity(i)
            }

        }
    }

    private fun maxNumberCount(): Int {
        return if (players!!.size % 2 == 0)
            players!!.size - 1
        else
            players!!.size
    }

    private fun choiceRoundsSwitchImplementation() {
        val linearLayout = findViewById<LinearLayout>(R.id.layout_set_round)
        val textView = findViewById<TextView>(R.id.auto_count_of_rounds)//new TextView(this);
        editText = EditText(this)
        editText!!.inputType = InputType.TYPE_CLASS_NUMBER
        editText!!.gravity = Gravity.START or Gravity.CENTER
        editText!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        editText!!.setTextColor(getColor(R.color.colorPrimaryDark))

        textView.text = getString(R.string.auto_count_of_rounds, optimalCountOfRounds)


        val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)

        val startMargins = (10 * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
        params.setMargins(startMargins, 0, 0, 0)


        switch1!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                editText!!.layoutParams = params
                linearLayout.removeView(textView)
                linearLayout.addView(editText)
            } else {
                linearLayout.removeView(editText)
                linearLayout.addView(textView)

            }
        }
    }



    private fun comparatorByStandardOrder() {
        players!!.sortWith(Comparator { o1, o2 ->
            var c = compare(o2.internationalRanking, o1.internationalRanking)
            if (c == 0) {
                c = compare(o2.polishRanking, o1.polishRanking)
                if (c == 0) {
                    c = o1.toString().compareTo(o2.toString())
                }
            }
            c
        })

        initListViewHeader()
    }

    private fun initListViewHeader() {
        val headerView = layoutInflater.inflate(R.layout.start_list_header, listView, false) as ViewGroup
        listView!!.addHeaderView(headerView)

        val adapter = ListAdapter(this, R.layout.row_layout, R.id.name_player_row_layout, getPlayerList())
        listView!!.adapter = adapter
    }

    private fun getPlayerList(): MutableList<String> {
        val list = ArrayList<String>()
        var counter = 1
        for (player in players!!) {
            val internationalRanking = if (player.internationalRanking == -1) getString(R.string.no_rank) else (player.internationalRanking.toString())
            val polishRanking = if (player.polishRanking == -1) getString(R.string.no_rank) else (player.polishRanking.toString())
            val coma = getString(R.string.comma)
            val tmp =  counter.toString() + getString(R.string.dot) + coma + player.toString() + coma + internationalRanking + coma + polishRanking
            list.add(tmp)
            counter++
        }
        return list
    }

    override fun onOkClicked(dialog: GeneralDialogFragment) {

    }

    override fun onCancelClicked(dialog: GeneralDialogFragment) {

    }
}
