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

//DistanceDialog to use this dialog in startButtonActivitiy
class DistanceDialog: DialogFragment(), DialogInterface.OnClickListener {

    private lateinit var distanceEditText: EditText
    private lateinit var userInputViewModel: UserInputViewModel
    private var distance: Float = 0f
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var dialog: Dialog
        val  bundle = arguments

        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_distance_dialog, null)

        userInputViewModel = ViewModelProvider(requireActivity()).get(UserInputViewModel::class.java)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        builder.setTitle("Distance")
        builder.setPositiveButton("ok", this)
        builder.setNegativeButton("cancel", this)
        dialog=builder.create();
        distanceEditText = view.findViewById(R.id.distanceEditText)
        return dialog
    }

    override fun onClick(dialog: DialogInterface?, item: Int) {
        if(item==DialogInterface.BUTTON_POSITIVE){
            Toast.makeText(requireActivity(), "ok Clicked", Toast.LENGTH_SHORT).show()
         var distanceString = distanceEditText.text.toString()
            if(distanceString==""){
                distance = 0f
            }
            else{
                distance = distanceString.toString().toFloat()
            }

            println("debug: in distanceDialog, this is the input for distance"+distance )
            //ok. this one. but who is actually setting the database?
            userInputViewModel.distance.value = distance
        }
        else if(item==DialogInterface.BUTTON_NEGATIVE)
            Toast.makeText(requireActivity(), "cancel Clicked", Toast.LENGTH_LONG).show()
    }
}