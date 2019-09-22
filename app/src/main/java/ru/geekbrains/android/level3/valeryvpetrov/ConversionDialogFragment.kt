package ru.geekbrains.android.level3.valeryvpetrov

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ConversionDialogFragment(private val onButtonClickListener: OnButtonClickListener) :
    DialogFragment() {

    interface OnButtonClickListener {

        fun onPositiveClick()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Conversion is in progress")
                .setPositiveButton("Stop") { dialog, id ->
                    onButtonClickListener.onPositiveClick()
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}