package pam.project.chesstournamentmanager.staticdata.dialogbox

import android.content.Context
import android.support.v4.app.DialogFragment

abstract class  BaseDialogFragment<T> : DialogFragment() {
    var mActivityInstance : T? = null

    fun getActivityInstance(): T? {return mActivityInstance}

    override fun onAttach(context: Context?) {
        mActivityInstance = context as T
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
        mActivityInstance = null
    }
}