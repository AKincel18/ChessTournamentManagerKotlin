package pam.project.chesstournamentmanager.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import android.util.TypedValue
import android.view.*
import android.widget.*
import pam.project.chesstournamentmanager.staticdata.adapters.SpinnerAdapter
import pam.project.chesstournamentmanager.R
import pam.project.chesstournamentmanager.matches.Match
import pam.project.chesstournamentmanager.matches.MatchResult
import pam.project.chesstournamentmanager.model.Player
import pam.project.chesstournamentmanager.staticdata.dialogbox.GeneralDialogFragment
import pam.project.chesstournamentmanager.staticdata.dialogbox.OnDialogFragmentClickListener
import pam.project.chesstournamentmanager.systems.Knockout
import pam.project.chesstournamentmanager.systems.SwissAlgorithm
import java.util.*

class Tournament : AppCompatActivity(), OnDialogFragmentClickListener {

    private var textViews: Array<TextView>? = null

    private var spinners: Array<Spinner>? = null

    private var swissAlgorithm: SwissAlgorithm? = null

    private var knockout: Knockout? = null

    private var titleTextView: TextView? = null

    private var matches: List<Match>? = null

    private var nextRoundButton: Button? = null

    private var myMenu: Menu? = null

    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null

    private var navigationView: NavigationView? = null

    private var chosenSystem: Boolean = false

    private var roundsNumber: Int = 0

    private val resultsTextView = ArrayList<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)

        nextRoundButton = findViewById(R.id.next_round_button)
        titleTextView = findViewById(R.id.round_count_text_view)

        val i = intent
        chosenSystem = i.getBooleanExtra(getString(R.string.chosen_system), true)
        val players = i.getSerializableExtra(getString(R.string.players)) as? List<Player>
        roundsNumber = i.getIntExtra(getString(R.string.rounds_number), 0)

        if (chosenSystem) {
            swissAlgorithm = SwissAlgorithm.getINSTANCE()

            if (swissAlgorithm == null) {

                val placeOrder = i.getBooleanExtra(getString(R.string.place_order), true)


                swissAlgorithm = SwissAlgorithm.initSwissAlgorithm(roundsNumber, placeOrder)
                swissAlgorithm!!.initTournamentPlayers(players as ArrayList<Player>)
                swissAlgorithm!!.drawFirstRound()

            }

            nextRoundButton()
        } else {

            knockout = Knockout.getINSTANCE()
            if (knockout == null) {
                knockout = Knockout.initKnockout(roundsNumber, players as ArrayList<Player>)
                knockout!!.initDraw()
            }

            nextRoundButtonKnockout()

        }

        initNavigationMenu()
        buildRoundsView()

        buildMenu()

    }

    private fun initNavigationMenu() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        actionBarDrawerToggle!!.isDrawerIndicatorEnabled = true


        drawerLayout.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle!!.syncState()

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        navigationView = findViewById(R.id.nav_view)
        myMenu = navigationView!!.menu

        buildColorMenu(R.id.rounds_menu, R.style.titleMenuStyle)

        if (!chosenSystem) {
            myMenu!!.getItem(1).isVisible = false
        }

        navigationView!!.setNavigationItemSelectedListener { item ->
            val currentRound: Int = if (chosenSystem) {
                swissAlgorithm!!.currentRound
            } else {
                knockout!!.currentRound
            }

            val id = item.itemId
            if (id != R.id.exit_menu) { //exit
                if (id == R.id.results_menu) { //results
                    if (currentRound != 1) {
                        val i = Intent(applicationContext, FinalResults::class.java)
                        startActivity(i)
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.not_finished_first_round),Toast.LENGTH_LONG).show()
                    }
                } else if (id >= 1) { //item rounds = (1, roundsNumber)
                    if (id < currentRound) { //previous round
                        refreshView(id, false)
                    } else if (id > currentRound) { //next round
                        Toast.makeText(applicationContext, getString(R.string.previous_rounds_not_finished), Toast.LENGTH_LONG ).show()
                    } else if (id == currentRound) { //current round
                        drawerLayout.closeDrawer(GravityCompat.START)
                    }
                }

            } else {
                val dialog = GeneralDialogFragment.exitDialogBox()
                dialog.show(supportFragmentManager, getString(R.string.title_warning))
            }

            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item) || actionBarDrawerToggle!!.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val dialog = GeneralDialogFragment.exitDialogBox()
        dialog.show(supportFragmentManager, getString(R.string.title_warning))
    }

    private fun refreshView(goToRound: Int, nextRound: Boolean) {
        if (chosenSystem) {
            changeTextButton()
            matches = swissAlgorithm!!.matches[goToRound - 1]

        }
        else {
            changeTextButtonKnockout()
            removeUselessTextViews()
            matches = knockout!!.matches[goToRound - 1]

            if (knockout!!.currentRound == knockout!!.roundsNumber)
                titleTextView!!.text = getString(R.string.final_round)
            else
                titleTextView!!.text = getString(R.string.phase_tournament, "1 / " + matches!!.size)
        }

        if (nextRound) {
            var count = 0
            var i = 0
            while (i < matches!!.size * 2) {
                if (!chosenSystem || !(count == matches!!.size - 1 && !swissAlgorithm!!.even)) {
                    textViews!![i].setTypeface(null, Typeface.NORMAL)
                    textViews!![i + 1].setTypeface(null, Typeface.NORMAL)
                    textViews!![i].setTextColor(getColor(R.color.colorPrimaryDark))
                    textViews!![i + 1].setTextColor(getColor(R.color.colorPrimaryDark))
                } else {
                    textViews!![i].setTextColor(getColor(R.color.winnerColor))
                    textViews!![i].setTypeface(null, Typeface.BOLD)
                    textViews!![i + 1].setTextColor(Color.RED)
                }

                textViews!![i].text = matches!![count].player1.toString()
                textViews!![i + 1].text = matches!![count].player2.toString()
                initSpinner(spinners!![count])
                count++
                i += 2
            }
        } else {
            val intent = Intent(applicationContext, RoundResults::class.java)
            intent.putExtra(getString(R.string.go_to_round), goToRound)
            startActivity(intent)
        }


    }

    private fun initSpinnersAndTextViews() {

        textViews = Array(matches!!.size * 2) {TextView(this) }
        spinners = Array(matches!!.size) { Spinner(this) }

        //matches
        for (i in 0 until matches!!.size * 2) {
            val textView = TextView(this)
            textView.id = i
            textView.setTypeface(null, Typeface.NORMAL)
            textView.setTextColor(getColor(R.color.colorPrimaryDark))
            textViews!![i] = textView
        }

        //spinners
        for (i in matches!!.indices) {
            val spinner = Spinner(this)
            spinner.id = i
            spinners!![i] = spinner

        }
    }

    private fun initSpinner(spinner: Spinner) {
        val list: MutableList<String>
        if (chosenSystem) {
            list = ArrayList(Arrays.asList(*this.resources.getStringArray(R.array.results_array)))
        } else {
            list = ArrayList(Arrays.asList(*this.resources.getStringArray(R.array.results_array_no_draw)))
        }

        list.add(getString(R.string.set_results)) //add hint
        val adapter = SpinnerAdapter(this, R.layout.spinner_result, list)

        adapter.setDropDownViewResource(R.layout.spinner_result)
        spinner.adapter = adapter
        spinner.setSelection(adapter.count)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (parent.selectedItem !== getString(R.string.set_results))
                    spinners!![spinner.id].setSelection(parent.selectedItemPosition)
                if (chosenSystem)
                    colorWinner(spinner.id)
                else
                    colorWinnerKnockout(spinner.id)


            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    private fun colorWinnerKnockout(matchNumber: Int) {
        when (spinners!![matchNumber].selectedItemPosition) {
            0 //WHITE_WON
            -> {
                textViews!![matchNumber * 2].setTextColor(getColor(R.color.winnerColor))
                textViews!![matchNumber * 2].setTypeface(null, Typeface.BOLD)
                textViews!![matchNumber * 2 + 1].setTextColor(Color.RED)
            }
            1 //BLACK_WON
            -> {
                textViews!![matchNumber * 2].setTextColor(Color.RED)
                textViews!![matchNumber * 2 + 1].setTextColor(getColor(R.color.winnerColor))
                textViews!![matchNumber * 2 + 1].setTypeface(null, Typeface.BOLD)
            }
        }
    }

    private fun colorWinner(matchNumber: Int) {
        when (spinners!![matchNumber].selectedItemPosition) {
            0 //WHITE_WON
            -> {
                textViews!![matchNumber * 2].setTextColor(getColor(R.color.winnerColor))
                textViews!![matchNumber * 2].setTypeface(null, Typeface.BOLD)
                textViews!![matchNumber * 2 + 1].setTextColor(Color.RED)
            }
            1 //DRAW
            -> {
                textViews!![matchNumber * 2].setTypeface(null, Typeface.ITALIC)
                textViews!![matchNumber * 2 + 1].setTypeface(null, Typeface.ITALIC)
            }
            2 //BLACK_WON
            -> {
                textViews!![matchNumber * 2].setTextColor(Color.RED)
                textViews!![matchNumber * 2 + 1].setTextColor(getColor(R.color.winnerColor))
                textViews!![matchNumber * 2 + 1].setTypeface(null, Typeface.BOLD)
            }
        }
    }

    private fun removeUselessTextViews() {
        var count = matches!!.size * 2
        for (i in matches!!.size - 1 downTo matches!!.size / 2) {
            count--
            textViews!![count].visibility = View.INVISIBLE
            count--
            textViews!![count].visibility = View.INVISIBLE
            spinners!![i].visibility = View.INVISIBLE
        }

        for (textView in resultsTextView) {
            textView.visibility = View.INVISIBLE
        }
    }

    private fun changeTextButtonKnockout() {
        if (knockout!!.currentRound == knockout!!.roundsNumber) {
            nextRoundButton!!.text = getString(R.string.show_results_button)
        }
    }

    private fun changeTextButton() {
        if (swissAlgorithm!!.currentRound == swissAlgorithm!!.roundsNumber) {
            nextRoundButton!!.text = getString(R.string.show_results_button)
        }
    }

    private fun buildColorMenu(rId: Int, rStyle: Int) {
        val rounds = myMenu!!.findItem(rId)
        val s = SpannableString(rounds.title)
        s.setSpan(TextAppearanceSpan(this, rStyle), 0, s.length, 0)
        rounds.title = s
    }

    private fun nextRoundButton() {
        nextRoundButton!!.setOnClickListener {
            val matchResults = getResult()
            if (matchResults != null) {
                swissAlgorithm!!.setResult(matchResults)

                if (swissAlgorithm!!.finishedTournament) {
                    val i = Intent(applicationContext, FinalResults::class.java)
                    startActivity(i)
                } else {
                    swissAlgorithm!!.drawNextRound()
                    titleTextView!!.text = getString(R.string.round_count_text_view, swissAlgorithm!!.currentRound)
                    val scrollView = findViewById<ScrollView>(R.id.scroll_view)
                    scrollView.scrollTo(0, 0)
                    refreshView(swissAlgorithm!!.currentRound, true)
                }

            } else {
                notAllResultsEnteredDialogBox()
            }
        }
    }

    private fun nextRoundButtonKnockout() {
        nextRoundButton!!.setOnClickListener {
            val matchResults = getResultKnockout()
            if (matchResults != null) {
                knockout!!.setResult(matchResults)

                if (knockout!!.isFinishedTournament) {
                    val i = Intent(applicationContext, FinalResults::class.java)
                    startActivity(i)
                } else {
                    val scrollView = findViewById<ScrollView>(R.id.scroll_view)
                    scrollView.scrollTo(0, 0)
                    buildRoundsView()
                }
            } else {
                notAllResultsEnteredDialogBox()
            }
        }
    }

    private fun notAllResultsEnteredDialogBox() {
        val generalDialogFragment = GeneralDialogFragment.newInstance(
            getString(R.string.title_error),
            getString(R.string.no_result_message_error),
            getString(R.string.exit_button)
        )
        generalDialogFragment.show(supportFragmentManager, getString(R.string.title_error))
    }

    private fun buildMenu() {
        val menuItem = myMenu!!.findItem(R.id.rounds_menu)
        val subMenu = menuItem.subMenu
        for (i in 1..roundsNumber) {
            subMenu.add(Menu.NONE, i, Menu.NONE, getString(R.string.round_count_text_view, i))
            buildColorMenu(i, R.style.subMenuRoundsStyle)
        }
        navigationView!!.invalidate()

    }

    private fun buildRoundsView() {
        val matchesRelativeLayout = findViewById<LinearLayout>(R.id.linear_layout_matches)
        if (chosenSystem) {
            changeTextButton()
            matches = swissAlgorithm!!.matches[swissAlgorithm!!.currentRound - 1]
        } else {
            changeTextButtonKnockout()
            matches = knockout!!.matches[knockout!!.currentRound - 1]
            matchesRelativeLayout.removeAllViews()

        }


        initSpinnersAndTextViews()
        if (chosenSystem) {
            titleTextView!!.text = getString(R.string.round_count_text_view, swissAlgorithm!!.currentRound)
        }
        else {
            if (knockout!!.currentRound == knockout!!.roundsNumber)
                titleTextView!!.text = getString(R.string.final_round)
            else
                titleTextView!!.text = getString(R.string.phase_tournament, "1 / " + matches!!.size)
        }


        //title
        titleTextView!!.textSize = 30.0f
        titleTextView!!.setTypeface(titleTextView!!.typeface, Typeface.BOLD)
        titleTextView!!.gravity = Gravity.CENTER_HORIZONTAL


        val paramsLeftTextSize = LinearLayout.LayoutParams(0, 50)
        paramsLeftTextSize.weight = 1f
        paramsLeftTextSize.setMargins(0, 5, 0, 5)

        val paramsSpinner = LinearLayout.LayoutParams(0, 50)
        paramsSpinner.weight = 1f
        paramsSpinner.setMargins(50, 5, 0, 5)

        val paramsRightTextSize = LinearLayout.LayoutParams(0, 50)
        paramsRightTextSize.weight = 1f
        paramsRightTextSize.setMargins(0, 5, 0, 5)


        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        params.setMargins(10, 5, 10, 5)
        linearLayout.layoutParams = params
        matchesRelativeLayout.addView(linearLayout)

        var matchNumber = 0
        var i = 0
        while (i < matches!!.size * 2) {
            val l = LinearLayout(this)
            l.orientation = LinearLayout.HORIZONTAL

            textViews!![i].layoutParams = paramsLeftTextSize
            textViews!![i].gravity = Gravity.START
            textViews!![i].text = matches!![matchNumber].player1.toString()
            textViews!![i].setAutoSizeTextTypeUniformWithConfiguration(1, 30, 2, TypedValue.COMPLEX_UNIT_SP)
            l.addView(textViews!![i])



            if (chosenSystem && matchNumber == matches!!.size - 1 && !swissAlgorithm!!.even) {
                l.addView(initByePlayer(i, 1))
            } else {

                if (matches!![matchNumber].player1!!.surname == getString(R.string.bye)) {
                    l.addView(initByePlayer(i, -1))
                } else if (matches!![matchNumber].player2!!.surname == getString(R.string.bye)) {
                    l.addView(initByePlayer(i, 1))
                } else {
                    initSpinner(spinners!![matchNumber])
                    spinners!![matchNumber].layoutParams = paramsSpinner
                    spinners!![matchNumber].gravity = Gravity.CENTER
                    l.addView(spinners!![matchNumber])
                }
            }


            textViews!![i + 1].gravity = Gravity.END
            textViews!![i + 1].text = matches!![matchNumber].player2.toString()
            textViews!![i + 1].layoutParams = paramsRightTextSize
            textViews!![i + 1].setAutoSizeTextTypeUniformWithConfiguration(1, 30, 2, TypedValue.COMPLEX_UNIT_SP)

            l.addView(textViews!![i + 1])



            linearLayout.addView(l)


            matchNumber++
            i += 2
        }
    }

    private fun initByePlayer(i: Int, flag: Int): View? {
        val textView = TextView(this)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)

        val paramsBye = LinearLayout.LayoutParams(0, 50)
        paramsBye.weight = 1f
        paramsBye.setMargins(0, 5, 0, 5)
        textView.layoutParams = paramsBye

        if (flag == -1) {
            textViews!![i + 1].setTextColor(getColor(R.color.winnerColor))
            textViews!![i + 1].setTypeface(null, Typeface.BOLD)
            textView.text = getString(R.string.black_won_result)
            textViews!![i].setTextColor(Color.RED)
        } else if (flag == 1) {
            textViews!![i].setTextColor(getColor(R.color.winnerColor))
            textViews!![i].setTypeface(null, Typeface.BOLD)
            textView.text = getString(R.string.white_won_result)
            textViews!![i + 1].setTextColor(Color.RED)
        }

        textView.gravity = Gravity.CENTER
        textView.setTextColor(getColor(R.color.colorPrimaryDark))
        resultsTextView.add(textView)

        return textView
    }

    private fun getResultKnockout(): ArrayList<MatchResult>? {
        val matchCount = matches!!.size

        val results = ArrayList<MatchResult>()
        for (i in 0 until matchCount) {
            if (matches!![i].player1!!.surname == getString(R.string.bye))
                results.add(MatchResult.BLACK_WON)
            else if (matches!![i].player2!!.surname == getString(R.string.bye)) {
                results.add(MatchResult.WHITE_WON)
            }
            else {
                val m = getMatchResultFromSpinnerKnockout(i)
                if (m != null)
                    results.add(m)
                else
                    return null
            }
        }
        return results
    }

    private fun getMatchResultFromSpinnerKnockout(pos: Int): MatchResult? {
        when (spinners!![pos].selectedItemPosition) {
            0 -> return MatchResult.WHITE_WON
            1 -> return MatchResult.BLACK_WON
        }
        return null //not selected result
    }


    private fun getResult(): ArrayList<MatchResult>? {
        val matchCount = if (swissAlgorithm!!.even) matches!!.size else matches!!.size - 1

        val results = ArrayList<MatchResult>()
        for (i in 0 until matchCount) {
            val m = getMatchResultFromSpinner(i)
            if (m != null)
                results.add(m)
            else
                return null
        }
        if (chosenSystem && !swissAlgorithm!!.even)
            results.add(MatchResult.WHITE_WON)

        return results
    }


    private fun getMatchResultFromSpinner(pos: Int): MatchResult? {
        when (spinners!![pos].selectedItemPosition) {
            0 -> return MatchResult.WHITE_WON
            1 -> return MatchResult.DRAW
            2 -> return MatchResult.BLACK_WON
        }
        return null //not selected result
    }






    override fun onOkClicked(dialog: GeneralDialogFragment) {
        if (dialog.tag.equals(getString(R.string.title_warning))) {
            val i = Intent(applicationContext, PlayersSelection::class.java)
            startActivity(i)
        }
    }

    override fun onCancelClicked(dialog: GeneralDialogFragment) {

    }
}
