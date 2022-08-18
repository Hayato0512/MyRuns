package com.example.myruns2

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider

//DurationDialog to use this dialog in startButtonActivitiy
class DurationDialog: DialogFragment(), DialogInterface.OnClickListener {
    private lateinit var userInputViewModel: UserInputViewModel
    private lateinit var durationEditText: EditText
    private var durationFinalInput: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var dialog: Dialog
        val  bundle = arguments

        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_duration_dialog, null)

        userInputViewModel = ViewModelProvider(requireActivity()).get(UserInputViewModel::class.java)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        builder.setTitle("Duration")
        builder.setPositiveButton("ok", this)
        builder.setNegativeButton("cancel", this)
        durationEditText = view.findViewById(R.id.durationEditText)
        dialog=builder.create();
        return dialog
    }

    override fun onClick(dialog: DialogInterface?, item: Int) {
        if(item==DialogInterface.BUTTON_POSITIVE) {
            Toast.makeText(requireActivity(), "ok Clicked", Toast.LENGTH_SHORT).show()
            val durationInput = durationEditText.text.toString()
            if(durationInput ==""){
                durationFinalInput = 0
            }
            else{
                durationFinalInput = durationInput.toString().toInt()
            }
            userInputViewModel.duration.value = durationFinalInput
        }
        else if(item==DialogInterface.BUTTON_NEGATIVE)
            Toast.makeText(requireActivity(), "cancel Clicked", Toast.LENGTH_LONG).show()
    }
}