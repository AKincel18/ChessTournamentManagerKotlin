package pam.project.chesstournamentmanager.activities

import android.app.Dialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import pam.project.chesstournamentmanager.R
import pam.project.chesstournamentmanager.database.MyDatabase
import pam.project.chesstournamentmanager.model.Player
import pam.project.chesstournamentmanager.staticdata.FormatDateToString.Companion.parseDateFromDatabase
import pam.project.chesstournamentmanager.staticdata.dialogbox.GeneralDialogFragment
import pam.project.chesstournamentmanager.staticdata.dialogbox.OnDialogFragmentClickListener
import java.util.ArrayList
import java.util.concurrent.Executors

class PlayersSelection : AppCompatActivity(), OnDialogFragmentClickListener {

    private val selectedAvailablePlayers = ArrayList<Player>()
    private val selectedChosenPlayers = ArrayList<Player>()

    private var chosenPlayerListView: ListView? = null
    private var allPlayersListView: ListView? = null

    private var database: MyDatabase? = null

    private var dialog: Dialog? = null

    companion object {
        private var availablePlayers = ArrayList<Player>()
        private var chosenPlayers = ArrayList<Player>()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_players_selection)

        chosenPlayerListView = findViewById(R.id.chosen_players_list_view)
        allPlayersListView = findViewById(R.id.available_players_list_view)

        database = MyDatabase.getInstance(this)

        val i = intent
        if (i.getSerializableExtra(getString(R.string.available_players)) != null) {
            availablePlayers = i.getSerializableExtra(getString(R.string.available_players)) as ArrayList<Player>
            initListView(allPlayersListView, availablePlayers, selectedAvailablePlayers)
        } else {
            fetchPlayers()
        }

        val message = i.getStringExtra(getString(R.string.toast_message))
        if ( message != null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        //buttons
        moveToChosenPlayers()
        moveToAvailablePlayers()
        addNewPlayer()
        configureTournament()
        aboutPlayer()
        removePlayer()
        editPlayer()

        selectedAll(R.id.select_all_checkbox, allPlayersListView, selectedAvailablePlayers)
        selectedAll(R.id.select_all_checkbox2, chosenPlayerListView, selectedChosenPlayers)

        initListView(chosenPlayerListView, chosenPlayers, selectedChosenPlayers)

        dialog = Dialog(this)
    }

    private fun initListView(listView: ListView?, playersList: ArrayList<Player>, selected: ArrayList<Player>) {
        listView!!.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        val adapter = ArrayAdapter<Player>(this, R.layout.list_players, R.id.simple_checked_text_view, playersList)
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedPlayer = parent.getItemAtPosition(position) as Player

            if (selected.contains(selectedPlayer))
                selected.remove(selectedPlayer) //uncheck player
            else
                selected.add(selectedPlayer)
        }
    }

    private fun fetchPlayers() {
        chosenPlayers.clear()
        availablePlayers.clear()

        Executors.newSingleThreadExecutor().execute {
            availablePlayers.addAll(database!!.playersDao().getAllPlayers())
            initListView(allPlayersListView, availablePlayers, selectedAvailablePlayers)
        }
    }

    private fun moveToChosenPlayers() { //'->' button
        val nextButton : Button = findViewById(R.id.move_to_chosen_players_button)
        nextButton.setOnClickListener {
            movement(chosenPlayers, availablePlayers, selectedAvailablePlayers)
            val checkBox : CheckBox = findViewById(R.id.select_all_checkbox)
            checkBox.isChecked = false

        }
    }


    private fun moveToAvailablePlayers() { //'<-' button
        val backButton = findViewById<Button>(R.id.move_to_available_players_button)
        backButton.setOnClickListener {
            movement(availablePlayers, chosenPlayers, selectedChosenPlayers)
            val checkBox = findViewById<CheckBox>(R.id.select_all_checkbox2)
            checkBox.isChecked = false
        }
    }

    private fun movement(add: ArrayList<Player>, remove: ArrayList<Player>, selected: ArrayList<Player>) {
        if (selected.isEmpty()) {
            Toast.makeText(this, getString(R.string.nothing_selected), Toast.LENGTH_LONG).show()
        }
        else {
            add.addAll(selected)
            remove.removeAll(selected)
            selected.clear()

            var adapter = ArrayAdapter<Player>(this, R.layout.list_players, R.id.simple_checked_text_view, availablePlayers)
            allPlayersListView!!.adapter = adapter

            adapter = ArrayAdapter<Player>(this, R.layout.list_players, R.id.simple_checked_text_view, chosenPlayers)
            chosenPlayerListView!!.adapter = adapter
        }
    }



    private fun addNewPlayer() {
        val addPlayerButton : Button = findViewById(R.id.add_player_button)
        addPlayerButton.setOnClickListener {
            val i = Intent(applicationContext, AddNewPlayer::class.java)
            i.putExtra(getString(R.string.available_players), availablePlayers)
            startActivity(i)
        }
    }

    private fun configureTournament() {
        val configureTournamentButton = findViewById<Button>(R.id.config_tournament_button)
        configureTournamentButton.setOnClickListener {
            if (chosenPlayers.size < 2) {
                val dialog = GeneralDialogFragment.newInstance(
                    getString(R.string.too_many_players_title),
                    getString(R.string.too_many_players_message),
                    getString(R.string.exit_button)
                )
                dialog.show(supportFragmentManager, getString(R.string.too_many_players_title))
            } else {
                if (chosenPlayers.size % 2 == 0) {
                    val i = Intent(applicationContext, ConfigureTournament::class.java)
                    i.putExtra(getString(R.string.players), chosenPlayers)
                    startActivity(i)
                } else {
                    val dialog = GeneralDialogFragment.newInstance(
                        getString(R.string.title_warning),
                        getString(R.string.odd_number_players),
                        getString(R.string.positive_button_warning))
                    dialog.show(supportFragmentManager, getString(R.string.title_warning))
                }
            }
        }
    }

    private fun aboutPlayer() {
        val aboutPlayerButton = findViewById<Button>(R.id.about_player)
        aboutPlayerButton.setOnClickListener {
            dialog!!.setContentView(R.layout.popup_about_player)
            val name = dialog!!.findViewById<TextView>(R.id.about_set_name_text_view)
            val date = dialog!!.findViewById<TextView>(R.id.about_set_date_of_birth_text_view)
            val polishRank = dialog!!.findViewById<TextView>(R.id.about_set_polish_ranking_text_view)
            val internationalRank = dialog!!.findViewById<TextView>(R.id.about_set_international_ranking_text_view)

            if (selectedAvailablePlayers.size == 1) {

                val p = selectedAvailablePlayers[0]
                name.text = p.toString()
                date.text = parseDateFromDatabase(p.dateOfBirth)
                polishRank.text = if (p.polishRanking != -1) p.polishRanking.toString() else getString(R.string.no_rank)
                internationalRank.text = if (p.internationalRanking != -1) p.internationalRanking.toString() else getString(R.string.no_rank)
                dialog!!.show()

            } else if (selectedAvailablePlayers.size > 1) {
                moreThanOnePlayerSelectedDialogBox()
            } else {
                noOnePlayerSelectedDialogBox()
            }
        }
    }

    private fun getDialog(player: Player): GeneralDialogFragment {
        val polishRanking = if (player.polishRanking != -1) (player.polishRanking).toString() else getString(R.string.no_rank)
        val internationalRanking = if (player.internationalRanking != -1) (player.internationalRanking.toString()) else getString(R.string.no_rank)

        return GeneralDialogFragment.newInstance(
            getString(R.string.remove_player_title_DB),
            getString(
                R.string.remove_player_warning,
                player.name,
                player.surname,
                parseDateFromDatabase(player.dateOfBirth),
                polishRanking,
                internationalRanking
            ),
            getString(R.string.positive_button_warning)
        )
    }

    private fun noOnePlayerSelectedDialogBox() {
        val dialog = GeneralDialogFragment.newInstance(
            getString(R.string.title_error),
            getString(R.string.no_one_selected),
            getString(R.string.positive_button_warning))
        dialog.show(supportFragmentManager, getString(R.string.about_player_db))
    }

    private fun moreThanOnePlayerSelectedDialogBox() {
        val dialog = GeneralDialogFragment.newInstance(
            getString(R.string.title_error),
            getString(R.string.too_many_player_selected),
            getString(R.string.positive_button_warning))
        dialog.show(supportFragmentManager, getString(R.string.about_player_db))
    }

    private fun removePlayer() {
        val removePlayerButton = findViewById<Button>(R.id.remove_player)
        removePlayerButton.setOnClickListener {
            if (selectedAvailablePlayers.size == 1) {
                val player = selectedAvailablePlayers[0]
                val dialog = getDialog(player)
                dialog.show(supportFragmentManager, getString(R.string.confirmation_removing_player))

            } else if (selectedAvailablePlayers.size > 1) {
                val dialog = GeneralDialogFragment.newInstance(
                    getString(R.string.title_warning),
                    getString(R.string.selected_player_remove),
                    getString(R.string.positive_button_warning)
                )
                dialog.show(supportFragmentManager, getString(R.string.confirmation_removing_player))
            } else {
                noOnePlayerSelectedDialogBox()
            }
        }
    }

    private fun editPlayer() {
        val editPlayerButton = findViewById<Button>(R.id.edit_player)
        editPlayerButton.setOnClickListener {
            if (selectedAvailablePlayers.size == 1) {
                availablePlayers.remove(selectedAvailablePlayers[0])
                val i = Intent(applicationContext, AddNewPlayer::class.java)
                i.putExtra(getString(R.string.available_players), availablePlayers)
                i.putExtra(getString(R.string.player), selectedAvailablePlayers[0])
                startActivity(i)
            } else if (selectedAvailablePlayers.size > 1) {
                moreThanOnePlayerSelectedDialogBox()
            } else {
                noOnePlayerSelectedDialogBox()
            }
        }
    }

    private fun selectedAll(idCheckBox: Int, listView: ListView?, list: ArrayList<Player>) {
        val selectAllCheckBox = findViewById<CheckBox>(idCheckBox)

        selectAllCheckBox.setOnClickListener {
            val itemCount = listView!!.count
            list.clear()

            if (selectAllCheckBox.isChecked) { //select all
                for (i in 0 until itemCount) {
                    listView.setItemChecked(i, true)
                    list.add(listView.getItemAtPosition(i) as Player)
                }
            } else {
                for (i in 0 until itemCount) { //deselect all
                    listView.setItemChecked(i, false)
                }
            }
        }
    }

    override fun onOkClicked(dialog: GeneralDialogFragment) {
        val playersList = ArrayList(selectedAvailablePlayers)

        if (dialog.tag.equals(getString(R.string.confirmation_removing_player))) {
            Executors.newSingleThreadExecutor().execute { database!!.playersDao().removePlayer(playersList) }

            availablePlayers.removeAll(selectedAvailablePlayers)
            selectedAvailablePlayers.clear()

            val adapter =
                ArrayAdapter(applicationContext, R.layout.list_players, R.id.simple_checked_text_view, availablePlayers)
            allPlayersListView!!.adapter = adapter
            if (playersList.size == 1)
                Toast.makeText(this, getString(R.string.removed_player), Toast.LENGTH_LONG).show()
            else
                Toast.makeText(this, getString(R.string.removed_players), Toast.LENGTH_LONG).show()
        }else if (dialog.tag.equals(getString(R.string.title_warning))){
            val i = Intent(applicationContext, ConfigureTournament::class.java)
            i.putExtra(getString(R.string.players), chosenPlayers)
            startActivity(i)
        }


    }

    override fun onCancelClicked(dialog: GeneralDialogFragment) {
        if (dialog.tag.equals(getString(R.string.confirmation_removing_player))) {
            selectedAvailablePlayers.clear()
            initListView(allPlayersListView, availablePlayers, selectedAvailablePlayers)
            val checkBox = findViewById<CheckBox>(R.id.select_all_checkbox)
            checkBox.isChecked = false
        }
    }
}
