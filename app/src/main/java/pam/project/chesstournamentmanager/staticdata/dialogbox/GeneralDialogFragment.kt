package pam.project.chesstournamentmanager.staticdata.dialogbox

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import pam.project.chesstournamentmanager.R
import pam.project.chesstournamentmanager.staticdata.Constants
import org.jetbrains.annotations.NotNull

class GeneralDialogFragment : BaseDialogFragment<OnDialogFragmentClickListener>(){

    companion object {
        fun newInstance(title : String, message : String, positiveBtn : String ) : GeneralDialogFragment {
            val frag  = GeneralDialogFragment()
            val args = Bundle()
            args.putString(Constants.TITLE, title)
            args.putString(Constants.MESSAGE, message)
            args.putString(Constants.POSITIVE_BTN, positiveBtn)
            frag.arguments = args
            return frag
        }
        fun exitDialogBox(): GeneralDialogFragment {
            return newInstance(Constants.WARNING_TITLE, Constants.EXIT_MESSAGE, Constants.POSITIVE_BUTTON)
        }
    }

    @NotNull
    @Override
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder : AlertDialog.Builder = AlertDialog.Builder(activity, R.style.alertDialog)
        builder.setTitle(arguments!!.getString(Constants.TITLE))
            .setMessage(arguments!!.getString(Constants.MESSAGE))
            .setPositiveButton(arguments!!.getString(Constants.POSITIVE_BTN)
            ) { dialog, whichButton ->
                // Positive button clicked
                getActivityInstance()!!.onOkClicked(this)
            }

        if (arguments!!.getString(Constants.TITLE).equals(getString(R.string.title_warning)) ||
                arguments!!.getString(Constants.TITLE).equals(getString(R.string.remove_player_title_DB)))
            builder.setNegativeButton(getString(R.string.negative_button_warning)
            ) { dialog, whichButton ->
                // negative button clicked
                getActivityInstance()!!.onCancelClicked(this)
            }
        return builder.create()
    }
}