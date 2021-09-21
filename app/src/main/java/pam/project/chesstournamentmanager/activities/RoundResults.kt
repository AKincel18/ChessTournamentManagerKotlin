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
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import pam.project.chesstournamentmanager.R
import pam.project.chesstournamentmanager.matches.Match
import pam.project.chesstournamentmanager.matches.MatchResult
import pam.project.chesstournamentmanager.staticdata.dialogbox.GeneralDialogFragment
import pam.project.chesstournamentmanager.staticdata.dialogbox.OnDialogFragmentClickListener
import pam.project.chesstournamentmanager.systems.Knockout
import pam.project.chesstournamentmanager.systems.SwissAlgorithm

class RoundResults : AppCompatActivity(), OnDialogFragmentClickListener {

    private var myMenu: Menu? = null

    private var allMatches: List<List<Match>>? = null

    private var currentRound: Int = 0

    private var textView: TextView? = null

    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null

    private var chosenSystem: Boolean = false

    private var isFinishedTournament: Boolean = false

    private var roundsNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_round_results)

        val i = intent

        textView = findViewById(R.id.previous_round_count_text_view)

        val currentView = i.getIntExtra(getString(R.string.go_to_round), 0)

        if (SwissAlgorithm.getINSTANCE() != null) {
            allMatches = SwissAlgorithm.getINSTANCE()!!.matches
            currentRound = SwissAlgorithm.getINSTANCE()!!.currentRound
            chosenSystem = true
            isFinishedTournament = SwissAlgorithm.getINSTANCE()!!.finishedTournament
            roundsNumber = SwissAlgorithm.getINSTANCE()!!.roundsNumber
        } 
        else if (Knockout.getINSTANCE() != null) {
            allMatches = Knockout.getINSTANCE()!!.matches
            currentRound = Knockout.getINSTANCE()!!.currentRound
            chosenSystem = false
            isFinishedTournament = Knockout.getINSTANCE()!!.isFinishedTournament
            roundsNumber = Knockout.getINSTANCE()!!.roundsNumber
        }



        initNavigationMenu()
        buildMenu()
        buildView(currentView)
    }

    private fun initNavigationMenu() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        actionBarDrawerToggle!!.isDrawerIndicatorEnabled = true


        drawerLayout.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle!!.syncState()


        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)


        myMenu = navigationView.menu

        if (!chosenSystem && !isFinishedTournament) {
            myMenu!!.getItem(1).isVisible = false
        }

        buildColorMenu(R.id.rounds_menu, R.style.titleMenuStyle)
        navigationView.setNavigationItemSelectedListener { item ->
            val id = item.itemId
            if (id != R.id.exit_menu) {
                if (id == currentRound && !isFinishedTournament) {
                    val i = Intent(applicationContext, Tournament::class.java)
                    i.putExtra(getString(R.string.chosen_system), chosenSystem)
                    i.putExtra(getString(R.string.rounds_number), roundsNumber)
                    startActivity(i)
                } else if (id <= currentRound) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val matchesRelativeLayout = findViewById<LinearLayout>(R.id.linear_layout_matches)
                    matchesRelativeLayout.removeAllViews()
                    buildView(id)
                } else if (id == R.id.results_menu) {
                    val i = Intent(applicationContext, FinalResults::class.java)
                    startActivity(i)
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



    private fun buildColorMenu(rId: Int, rStyle: Int) {
        val rounds = myMenu!!.findItem(rId)
        val s = SpannableString(rounds.title)
        s.setSpan(TextAppearanceSpan(this, rStyle), 0, s.length, 0)
        rounds.title = s
    }


    private fun buildMenu() {
        val menuItem = myMenu!!.findItem(R.id.rounds_menu)
        val subMenu = menuItem.subMenu
        for (i in 1..currentRound) {
            subMenu.add(Menu.NONE, i, Menu.NONE, getString(R.string.round_count_text_view, i))
            buildColorMenu(i, R.style.subMenuRoundsStyle)
        }
    }
    
    
    private fun buildView(currentRound: Int) {
        if (chosenSystem)
            textView!!.text = getString(R.string.round_count_text_view, currentRound)
        else {
            if (currentRound == Knockout.getINSTANCE()!!.roundsNumber)
                textView!!.text = getString(R.string.final_round)
            else
                textView!!.text = getString(R.string.phase_tournament, "1 / " + allMatches!!.get(currentRound - 1).size)
        }

        val matchesRelativeLayout = findViewById<LinearLayout>(R.id.linear_layout_matches)

        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL


        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        params.setMargins(10, 5, 10, 5)
        linearLayout.layoutParams = params
        matchesRelativeLayout.addView(linearLayout)

        val paramsNo = LinearLayout.LayoutParams(0, 50, 0.05f)
        paramsNo.setMargins(0, 5, 5, 5)


        val paramsPlayer1 = LinearLayout.LayoutParams(0, 50, 0.35f)
        paramsPlayer1.setMargins(0, 5, 5, 5)

        val paramsResult = LinearLayout.LayoutParams(0, 50, 0.25f)
        paramsResult.setMargins(15, 5, 0, 5)

        val matches = allMatches!!.get(currentRound - 1)

        for (i in matches.indices) {
            val l = LinearLayout(this)
            l.orientation = LinearLayout.HORIZONTAL

            val noTextView = TextView(this)
            noTextView.text = getString(R.string.no, i + 1)
            noTextView.setAutoSizeTextTypeUniformWithConfiguration(1, 30, 1, TypedValue.COMPLEX_UNIT_SP)
            noTextView.gravity = Gravity.START
            noTextView.setTextColor(getColor(R.color.colorPrimaryDark))
            noTextView.layoutParams = paramsNo
            l.addView(noTextView)

            val player1TextView = TextView(this)
            player1TextView.text = matches[i].player1.toString()
            player1TextView.setAutoSizeTextTypeUniformWithConfiguration(1, 30, 1, TypedValue.COMPLEX_UNIT_SP)
            player1TextView.gravity = Gravity.START
            player1TextView.layoutParams = paramsPlayer1
            l.addView(player1TextView)

            val player2TextView = TextView(this)
            player2TextView.text = matches[i].player2.toString()
            player2TextView.setAutoSizeTextTypeUniformWithConfiguration(1, 30, 1, TypedValue.COMPLEX_UNIT_SP)
            player2TextView.gravity = Gravity.START
            player2TextView.layoutParams = paramsPlayer1
            l.addView(player2TextView)


            val resultTextView = TextView(this)
            resultTextView.text = getMatchResult(player1TextView, player2TextView, matches[i])
            resultTextView.setAutoSizeTextTypeUniformWithConfiguration(1, 30, 1, TypedValue.COMPLEX_UNIT_SP)
            resultTextView.gravity = Gravity.START
            resultTextView.setTextColor(getColor(R.color.colorPrimaryDark))
            resultTextView.layoutParams = paramsResult
            l.addView(resultTextView)


            linearLayout.addView(l)
        }
    }

    private fun getMatchResult(p1: TextView, p2: TextView, match: Match): String? {
        when (match.matchResult) {
            MatchResult.WHITE_WON -> {
                p2.setTextColor(Color.RED)
                p1.setTextColor(getColor(R.color.winnerColor))
                p1.setTypeface(null, Typeface.BOLD)
                return getString(R.string.white_won_result)
            }
            MatchResult.DRAW -> {
                p1.setTypeface(null, Typeface.ITALIC)
                p2.setTypeface(null, Typeface.ITALIC)
                p1.setTextColor(getColor(R.color.colorPrimaryDark))
                p2.setTextColor(getColor(R.color.colorPrimaryDark))
                return getString(R.string.draw_result)
            }
            MatchResult.BLACK_WON -> {
                p1.setTextColor(Color.RED)
                p2.setTypeface(null, Typeface.BOLD)
                p2.setTextColor(getColor(R.color.winnerColor))
                return getString(R.string.black_won_result)
            }
        }
        return null
    }


    override fun onOkClicked(dialog: GeneralDialogFragment) {
        val i = Intent(applicationContext, PlayersSelection::class.java)
        startActivity(i)
    }

    override fun onCancelClicked(dialog: GeneralDialogFragment) {

    }
}
