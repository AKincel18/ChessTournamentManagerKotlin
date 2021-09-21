package pam.project.chesstournamentmanager.activities

import android.content.Intent
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import pam.project.chesstournamentmanager.R
import pam.project.chesstournamentmanager.model.TournamentPlayer
import pam.project.chesstournamentmanager.staticdata.dialogbox.GeneralDialogFragment
import pam.project.chesstournamentmanager.staticdata.dialogbox.OnDialogFragmentClickListener
import pam.project.chesstournamentmanager.systems.Knockout
import pam.project.chesstournamentmanager.systems.SwissAlgorithm
import java.util.*

class FinalResults : AppCompatActivity(), OnDialogFragmentClickListener {


    private var currentRound: Int = 0

    private var myMenu: Menu? = null

    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null

    private var chosenSystem: Boolean = false

    private var isFinishedTournament: Boolean = false

    private var roundsNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_results)

        if (SwissAlgorithm.getINSTANCE() != null) {
            chosenSystem = true
            isFinishedTournament = SwissAlgorithm.getINSTANCE()!!.finishedTournament
            currentRound = SwissAlgorithm.getINSTANCE()!!.currentRound
            roundsNumber = SwissAlgorithm.getINSTANCE()!!.roundsNumber
        }
        else if (Knockout.getINSTANCE() != null) {
            chosenSystem = false
            isFinishedTournament = Knockout.getINSTANCE()!!.isFinishedTournament

            currentRound = Knockout.getINSTANCE()!!.currentRound
            roundsNumber = Knockout.getINSTANCE()!!.roundsNumber
        }

        val textView = findViewById<TextView>(R.id.current_results)
        if (isFinishedTournament) {
            textView.text = getString(R.string.final_result)
        } else {
            textView.text = getString(R.string.current_results, currentRound - 1)
        }

        initNavigationView()
        buildMenu()
        buildView()
    }


    private fun initNavigationView() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        actionBarDrawerToggle!!.isDrawerIndicatorEnabled = true


        drawerLayout.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle!!.syncState()

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        myMenu = navigationView.menu
        buildColorMenu(R.id.rounds_menu, R.style.titleMenuStyle)

        navigationView.setNavigationItemSelectedListener { item ->
            val id = item.itemId
            if (id != R.id.exit_menu) {
                if (id == currentRound && !isFinishedTournament) {
                    val i = Intent(applicationContext, Tournament::class.java)
                    i.putExtra(getString(R.string.rounds_number), roundsNumber)
                    startActivity(i)
                } else if (id <= currentRound) {
                    val i = Intent(applicationContext, RoundResults::class.java)
                    i.putExtra(getString(R.string.go_to_round), id)
                    startActivity(i)
                } else if (id == R.id.results_menu) {
                    drawerLayout.closeDrawer(Gravity.START)
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

    private fun buildMenu() {
        val menuItem = myMenu!!.findItem(R.id.rounds_menu)
        val subMenu = menuItem.subMenu
        for (i in 1..currentRound) {
            subMenu.add(Menu.NONE, i, Menu.NONE, getString(R.string.round_count_text_view, i))
            buildColorMenu(i, R.style.subMenuRoundsStyle)
        }
    }

    private fun buildColorMenu(rId: Int, rStyle: Int) {
        val rounds = myMenu!!.findItem(rId)
        val s = SpannableString(rounds.title)
        s.setSpan(TextAppearanceSpan(this, rStyle), 0, s.length, 0)
        rounds.title = s
    }

    private fun buildHeader() {

        val paramsBuchholzHeader = LinearLayout.LayoutParams(250, ViewGroup.LayoutParams.MATCH_PARENT)
        paramsBuchholzHeader.setMargins(0, 0, 0, 5)

        val buchholzHeaderTextView = TextView(this)
        buchholzHeaderTextView.setTextColor(getColor(R.color.colorAccent))
        buchholzHeaderTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
        buchholzHeaderTextView.gravity = Gravity.START
        buchholzHeaderTextView.layoutParams = paramsBuchholzHeader

        if (SwissAlgorithm.getINSTANCE()!!.placeOrder) {
            buchholzHeaderTextView.text = getString(R.string.buchholz_header_text_view)
        } else {
            buchholzHeaderTextView.text = getString(R.string.median_buchholz_header_text_view)
        }

        val headerLayout = findViewById<LinearLayout>(R.id.header_results_layout)
        headerLayout.addView(buchholzHeaderTextView)
    }


    private fun buildView() {
        val matchesRelativeLayout = findViewById<LinearLayout>(R.id.linear_layout_matches)

        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        params.setMargins(10, 5, 10, 5)
        linearLayout.layoutParams = params
        matchesRelativeLayout.addView(linearLayout)


        val paramsPosition = LinearLayout.LayoutParams(100, 50)
        paramsPosition.setMargins(0, 5, 0, 5)

        val paramsPlayerTextView = LinearLayout.LayoutParams(450, 50)
        paramsPlayerTextView.setMargins(0, 5, 5, 5)

        val paramsPointsTextView = LinearLayout.LayoutParams(250, 50)
        paramsPointsTextView.setMargins(0, 5, 0, 5)

        val playerList: List<TournamentPlayer>
        if (chosenSystem) {
            if (SwissAlgorithm.getINSTANCE()!!.finishedTournament) {
                buildHeader()
            }

            playerList = SwissAlgorithm.getINSTANCE()!!.tournamentPlayers!!
        } else {
            val textView = findViewById<TextView>(R.id.results_text_view)
            textView.visibility = View.INVISIBLE
            playerList = Knockout.getINSTANCE()!!.sortPlayers()
        }

        for (i in playerList.indices) {
            if (playerList[i].surname == getString(R.string.bye))
                continue

            val l = LinearLayout(this)
            l.orientation = LinearLayout.HORIZONTAL

            val positionTextView = TextView(this)
            positionTextView.setAutoSizeTextTypeUniformWithConfiguration(1, 30, 2, TypedValue.COMPLEX_UNIT_SP)
            positionTextView.setTextColor(getColor(R.color.colorPrimaryDark))
            positionTextView.gravity = Gravity.START

            if (chosenSystem) {
                positionTextView.text = getString(R.string.no, i + 1)
            } else {
                positionTextView.text = getPosition(i).toString()
            }
            positionTextView.layoutParams = paramsPosition
            l.addView(positionTextView)

            val playerTextView = TextView(this)
            playerTextView.setAutoSizeTextTypeUniformWithConfiguration(1, 30, 2, TypedValue.COMPLEX_UNIT_SP)
            playerTextView.setTextColor(getColor(R.color.colorPrimaryDark))
            playerTextView.gravity = Gravity.START
            playerTextView.text = playerList[i].toString()
            playerTextView.layoutParams = paramsPlayerTextView

            if (i == 0) {
                playerTextView.setTextColor(getColor(R.color.colorGolden))
                playerTextView.setTypeface(null, Typeface.BOLD)
            }
            if (i == 1) {
                playerTextView.setTextColor(getColor(R.color.colorSilver))
                playerTextView.setTypeface(null, Typeface.BOLD)
            }
            if (i == 2) {
                playerTextView.setTextColor(getColor(R.color.colorBronze))
                playerTextView.setTypeface(null, Typeface.BOLD)
            }

            l.addView(playerTextView)



            if (chosenSystem) {

                val pointTextView = TextView(this)
                pointTextView.setAutoSizeTextTypeUniformWithConfiguration(1, 30, 2, TypedValue.COMPLEX_UNIT_SP)
                pointTextView.setTextColor(getColor(R.color.colorPrimaryDark))
                pointTextView.gravity = Gravity.START
                pointTextView.layoutParams = paramsPointsTextView

                pointTextView.text = String.format(
                    Locale(getString(R.string.locale)),
                    getString(R.string.format_float),
                    playerList[i].points
                )
                l.addView(pointTextView)

                if (SwissAlgorithm.getINSTANCE()!!.finishedTournament) {


                    val buchholzPointsTextView = TextView(this)
                    buchholzPointsTextView.setTextColor(getColor(R.color.colorPrimaryDark))
                    buchholzPointsTextView.gravity = Gravity.START
                    buchholzPointsTextView.setAutoSizeTextTypeUniformWithConfiguration(
                        1,
                        30,
                        2,
                        TypedValue.COMPLEX_UNIT_SP
                    )
                    buchholzPointsTextView.layoutParams = paramsPointsTextView

                    if (SwissAlgorithm.getINSTANCE()!!.placeOrder) {
                        buchholzPointsTextView.text =
                                String.format(
                                    Locale(getString(R.string.locale)),
                                    getString(R.string.format_float),
                                    playerList[i].buchholzPoints
                                )
                    } else {
                        buchholzPointsTextView.text =
                                String.format(
                                    Locale(getString(R.string.locale)),
                                    getString(R.string.format_float),
                                    playerList[i].medianBuchholzMethod
                                )
                    }

                    l.addView(buchholzPointsTextView)
                }
            }

            linearLayout.addView(l)

        }



    }


    private fun getPosition(pos: Int): Int {
        if (pos == 0)
            return 1
        else {
            var lowerBound = 1
            var upperBound = 2
            var exponent = 1

            var find = false
            do {
                if (pos in lowerBound..(upperBound - 1)) {
                    find = true
                } else {
                    lowerBound = upperBound
                    exponent++
                    upperBound = Math.pow(2.0, exponent.toDouble()).toInt()
                }
            } while (!find)
            return Math.pow(2.0, (exponent - 1).toDouble()).toInt() + 1
        }
    }

    override fun onOkClicked(dialog: GeneralDialogFragment) {
        val i = Intent(applicationContext, PlayersSelection::class.java)
        startActivity(i)
    }
    override fun onCancelClicked(dialog: GeneralDialogFragment) {

    }


}
