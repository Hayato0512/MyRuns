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

//CommentDialog to use this dialog in startButtonActivitiy
class CommentDialog: DialogFragment(), DialogInterface.OnClickListener {
    lateinit var commentEditText: EditText
    lateinit var userInputViewModel: UserInputViewModel
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var dialog: Dialog
        val bundle = arguments

        //inflate the xml
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_comment_dialog, null)


        userInputViewModel = ViewModelProvider(requireActivity()).get(UserInputViewModel::class.java)




        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        builder.setTitle("Calories")
        builder.setPositiveButton("ok", this)
        builder.setNegativeButton("cancel", this)
        dialog = builder.create();
        commentEditText = view.findViewById(R.id.commentDialogInput)
        println("debug: successfully made a variable commentInput")

        return dialog
    }

    //function for onClick for buttons
    override fun onClick(dialog: DialogInterface?, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE) {
            Toast.makeText(requireActivity(), "ok Clicked", Toast.LENGTH_SHORT).show()

            val commentInput = commentEditText.text.toString()
            userInputViewModel.comment.value = commentInput
            println("debug: commentEditText.text is like this" +userInputViewModel.comment.value )
        }
        else if (item == DialogInterface.BUTTON_NEGATIVE)
            Toast.makeText(requireActivity(), "cancel Clicked", Toast.LENGTH_LONG).show()
    }
}
