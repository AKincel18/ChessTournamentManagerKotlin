package pam.project.chesstournamentmanager.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import pam.project.chesstournamentmanager.R
import pam.project.chesstournamentmanager.database.MyDatabase
import pam.project.chesstournamentmanager.model.Player
import pam.project.chesstournamentmanager.staticdata.FormatDateToString.Companion.parseDateFromDatabase
import pam.project.chesstournamentmanager.staticdata.FormatDateToString.Companion.parseDateFromDatePicker
import pam.project.chesstournamentmanager.staticdata.dialogbox.GeneralDialogFragment
import pam.project.chesstournamentmanager.staticdata.dialogbox.OnDialogFragmentClickListener
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class AddNewPlayer : AppCompatActivity(), OnDialogFragmentClickListener {

    private var pickDateTextView: TextView? = null

    private var dateSetListener: DatePickerDialog.OnDateSetListener? = null

    private var allPlayers: ArrayList<Player>? = null

    private var database: MyDatabase? = null

    private var formatDate: Date? = null

    private var addNewPlayer: Boolean = false

    private var editPlayer: Player? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.popup_add_new_player)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        window.setLayout((width * .6).toInt(), (height * .66).toInt())

        val params = window.attributes
        params.gravity = Gravity.CENTER
        params.x = 0
        params.y = 0

        window.attributes = params

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        database = MyDatabase.getInstance(this)

        val i = intent
        allPlayers = i.getSerializableExtra(getString(R.string.available_players)) as ArrayList<Player>

        editPlayer = i.getSerializableExtra(getString(R.string.player)) as? Player
        addNewPlayer = editPlayer == null


        if (!addNewPlayer) { //edit player popup
            setValues(editPlayer!!)
            val textView = findViewById<TextView>(R.id.add_edit_text_view)
            textView.text = getString(R.string.edit_player)
        }

        confirmNewPlayer()
        pickDateListener()
        close()
    }

    private fun pickDateListener() {
        pickDateTextView = findViewById(R.id.pick_date_text_view)
        pickDateTextView!!.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val dialog = DatePickerDialog(
                this@AddNewPlayer,
                R.style.datePicker,
                dateSetListener,
                year, month, day
            )

            dialog.show()
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val date = parseDateFromDatePicker(year, month, dayOfMonth)
            pickDateTextView!!.text = date
        }
    }

    //set values to fields
    private fun setValues(player: Player) {
        val name = findViewById<EditText>(R.id.name_edit_text)
        name.setText(player.name)
        
        val surname = findViewById<EditText>(R.id.surname_edit_text)
        surname.setText(player.surname)

        val date = findViewById<TextView>(R.id.pick_date_text_view)
        date.text = parseDateFromDatabase(player.dateOfBirth)

        val polishRank = findViewById<EditText>(R.id.polish_ranking_number)
        if (player.polishRanking != -1) {
            polishRank.setText((player.polishRanking).toString())
        } else {
            polishRank.hint = getString(R.string.no_rank)
        }

        val internationalRank = findViewById<EditText>(R.id.international_ranking_number)
        if (player.internationalRanking != -1) {
            internationalRank.setText((player.internationalRanking).toString())
        } else {
            internationalRank.hint = getString(R.string.no_rank)
        }
    }

    private fun confirmNewPlayer() {
        val confirmPlayerButton = findViewById<Button>(R.id.confirm_new_player_button)
        confirmPlayerButton.setOnClickListener {
            
            val name = findViewById<EditText>(R.id.name_edit_text)
            val surname = findViewById<EditText>(R.id.surname_edit_text)
            val date = findViewById<TextView>(R.id.pick_date_text_view)
            val polishRanking = findViewById<EditText>(R.id.polish_ranking_number)
            val internationalRanking = findViewById<EditText>(R.id.international_ranking_number)

            val format = SimpleDateFormat(getString(R.string.format_date), Locale(getString(R.string.locale)))
            var error = false
            formatDate = Date()
            try {
                formatDate = format.parse(date.text.toString())
            } catch (exc: ParseException) {
                val generalDialogFragment = GeneralDialogFragment.newInstance(
                    getString(R.string.title_error),
                    getString(R.string.message_error),
                    getString(R.string.exit_button)
                )
                generalDialogFragment.show(supportFragmentManager, getString(R.string.title_error))
                error = true

            }
            if (!error) {
                
                val player = Player(
                    name.text.toString(),
                    surname.text.toString(),
                    formatDate!!
                )

                try {
                    player.polishRanking = Integer.valueOf(polishRanking.text.toString())
                } catch (e: NumberFormatException) {
                    player.polishRanking = -1
                }

                try {
                    player.internationalRanking = Integer.valueOf(internationalRanking.text.toString())
                } catch (e: NumberFormatException) {
                    player.internationalRanking = -1
                }

                if (player.name == getString(R.string.empty_string) || player.surname == getString(R.string.empty_string)) {
                    val dialog = GeneralDialogFragment.newInstance(
                        getString(R.string.title_error),
                        getString(R.string.required_fields),
                        getString(R.string.exit_button)
                    )
                    dialog.show(supportFragmentManager, getString(R.string.title_error))

                } else {
                    if (addNewPlayer) {
                        saveNewPlayer(player)
                    } else {
                        updatePlayer(player)
                    }
                }
                
            }

        }
    }

    private fun saveNewPlayer(player: Player) {
        Executors.newSingleThreadExecutor().execute { database!!.playersDao().insertPlayer(player) }

        val i = Intent(applicationContext, PlayersSelection::class.java)
        if (allPlayers == null)
            allPlayers = ArrayList()

        allPlayers!!.add(player)
        i.putExtra(getString(R.string.available_players), allPlayers)
        i.putExtra(getString(R.string.toast_message), getString(R.string.added_player))
        startActivity(i)
    }

    private fun updatePlayer(player: Player) {
        editPlayer!!.name = player.name
        editPlayer!!.surname = player.surname
        editPlayer!!.polishRanking = player.polishRanking
        editPlayer!!.internationalRanking = player.internationalRanking
        editPlayer!!.dateOfBirth = player.dateOfBirth

        Executors.newSingleThreadExecutor().execute { database!!.playersDao().updatePlayer(editPlayer!!) }

        val i = Intent(applicationContext, PlayersSelection::class.java)
        if (allPlayers == null)
            allPlayers = ArrayList()

        allPlayers!!.add(editPlayer!!)
        i.putExtra(getString(R.string.available_players), allPlayers)
        i.putExtra(getString(R.string.toast_message), getString(R.string.edited_player))
        startActivity(i)
    }



    private fun close() {
        val closeButton : Button = findViewById(R.id.close_button)
        closeButton.setOnClickListener {
            showDialogBox()
        }
    }


    override fun onBackPressed() {
        showDialogBox()
    }

    private fun showDialogBox() {
        val generalDialogFragment = GeneralDialogFragment.newInstance(
            getString(R.string.title_warning),
            getString(R.string.information_warning),
            getString(R.string.positive_button_warning)
        )
        generalDialogFragment.show(supportFragmentManager, getString(R.string.title_warning))
    }



    override fun onOkClicked(dialog: GeneralDialogFragment) {
        if (dialog.arguments!!.getString(getString(R.string.title)) == getString(R.string.title_warning)){
            val i = Intent(this, PlayersSelection::class.java)
            if (!addNewPlayer) {
                allPlayers!!.add(editPlayer!!)
                i.putExtra(getString(R.string.toast_message), getString(R.string.not_edit_player))
            } else {
                i.putExtra(getString(R.string.toast_message), getString(R.string.not_added_player))
            }

            i.putExtra(getString(R.string.available_players), allPlayers)
            startActivity(i)
        }
    }

    override fun onCancelClicked(dialog: GeneralDialogFragment) {
    }
}
