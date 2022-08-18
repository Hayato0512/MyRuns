package com.example.myruns2

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment

//DateDialog to use this dialog in startButtonActivitiy
class DateDialog:DialogFragment(),DialogInterface.OnClickListener {
    companion object{
        const val TEST_DIALOG = 1
        const val DIALOG_KEY = "key"

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var dialog: Dialog
        val bundle = arguments
        if(bundle?.getInt(DIALOG_KEY)== TEST_DIALOG){

            //inflate the xml
            val view = requireActivity().layoutInflater.inflate(R.layout.fragment_data_dialog, null)

            val builder = AlertDialog.Builder(requireActivity())

            builder.setView(view)
            builder.setTitle("Date")
            builder.setPositiveButton("ok", this)
            builder.setNegativeButton("cancel", this)
            dialog = builder.create()

        }

        return dialog
    }

    //function for onClick for buttons
    override fun onClick(dialog: DialogInterface, item: Int){
        if(item==DialogInterface.BUTTON_POSITIVE)
            Toast.makeText(requireActivity(), "ok Clicked", Toast.LENGTH_SHORT).show()
        else if(item==DialogInterface.BUTTON_NEGATIVE)
            Toast.makeText(requireActivity(), "cancel Clicked", Toast.LENGTH_LONG).show()

    }

}