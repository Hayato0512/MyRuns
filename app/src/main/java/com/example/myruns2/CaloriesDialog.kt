package com.example.myruns2

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider

//CaloriesDialog to use this dialog in startButtonActivitiy
class CaloriesDialog: DialogFragment(), DialogInterface.OnClickListener {
private lateinit var userInputViewModel:UserInputViewModel
    private var caloriesFinalInput:Float = 0f
 lateinit var caloriesEditText: EditText
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var dialog: Dialog
        val  bundle = arguments
           // caloriesDialogInput
        //inflate the xml
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_calories_dialog, null)

            userInputViewModel = ViewModelProvider(requireActivity()).get(UserInputViewModel::class.java)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        builder.setTitle("Calories")
        builder.setPositiveButton("ok", this)
        builder.setNegativeButton("cancel", this)
        dialog=builder.create();
        caloriesEditText= view.findViewById(R.id.caloriesDialogInput)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    //function for onClick for buttons
    override fun onClick(dialog: DialogInterface?, item: Int) {
        if(item==DialogInterface.BUTTON_POSITIVE) {
            Toast.makeText(requireActivity(), "ok Clicked", Toast.LENGTH_SHORT).show()

            val caloriesInput = caloriesEditText.text.toString()
            if(caloriesInput==""){
                caloriesFinalInput = 0f
            }
            else{
                caloriesFinalInput = caloriesInput.toString().toFloat()
            }
            userInputViewModel.calories.value = caloriesFinalInput
        }
        else if(item==DialogInterface.BUTTON_NEGATIVE)
            Toast.makeText(requireActivity(), "cancel Clicked", Toast.LENGTH_LONG).show()
    }
}