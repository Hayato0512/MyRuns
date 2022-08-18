package com.example.myruns2

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment

class PhotoChangeDialog: DialogFragment(), DialogInterface.OnClickListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var dialog: Dialog
        val bundle = arguments

        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_comment_dialog, null)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        builder.setTitle("Calories")
        builder.setPositiveButton("ok", this)
        builder.setNegativeButton("cancel", this)
        dialog = builder.create();
        return dialog
    }

    override fun onClick(dialog: DialogInterface?, item: Int) {
        if (item == DialogInterface.BUTTON_POSITIVE)
            Toast.makeText(requireActivity(), "ok Clicked", Toast.LENGTH_SHORT).show()
        else if (item == DialogInterface.BUTTON_NEGATIVE)
            Toast.makeText(requireActivity(), "cancel Clicked", Toast.LENGTH_LONG).show()
    }
}
