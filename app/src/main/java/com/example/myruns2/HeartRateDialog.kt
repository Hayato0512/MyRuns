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

class HeartRateDialog: DialogFragment(), DialogInterface.OnClickListener {

    private lateinit var userInputViewModel:UserInputViewModel
    private lateinit var heartRateEditText: EditText
    private var heartRateInputToPass=0;
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var dialog: Dialog
        val  bundle = arguments

        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_heartrate_dialog, null)

        userInputViewModel = ViewModelProvider(requireActivity()).get(UserInputViewModel::class.java)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        builder.setTitle("Heart Rate")
        builder.setPositiveButton("ok", this)
        builder.setNegativeButton("cancel", this)
        dialog=builder.create();
        heartRateEditText = view.findViewById(R.id.heartRateInput)
        return dialog
    }

    override fun onClick(dialog: DialogInterface?, item: Int) {
        if(item==DialogInterface.BUTTON_POSITIVE) {
            Toast.makeText(requireActivity(), "ok Clicked", Toast.LENGTH_SHORT).show()
            val heartRateInput = heartRateEditText.text.toString()
            if(heartRateInput==""){
                heartRateInputToPass = 0
            }
            else{
                heartRateInputToPass = heartRateInput.toString().toInt()
            }
            userInputViewModel.heartRate.value = heartRateInputToPass
        }
        else if(item==DialogInterface.BUTTON_NEGATIVE)
            Toast.makeText(requireActivity(), "cancel Clicked", Toast.LENGTH_LONG).show()
    }
}