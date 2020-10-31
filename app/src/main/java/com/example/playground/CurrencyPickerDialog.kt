package com.example.playground

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import java.lang.Integer.max


class CurrencyPickerDialog(private var currentValue: Int, private var pickerList: List<String>) : DialogFragment() {
    private lateinit var onValueChangeListener : OnValueChangeListener

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val picker = NumberPicker(activity)

        picker.minValue = 0
        picker.maxValue = max(0,pickerList.size - 1)
        picker.displayedValues = pickerList.toTypedArray()
        picker.value = currentValue
        picker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(activity)
        builder.setTitle(getString(R.string.pickerTitle))
        builder.setPositiveButton(getString(R.string.ok)) { _, _ ->
            onValueChangeListener.onValueChange(
                picker,
                picker.value, picker.value
            )
        }
        builder.setNegativeButton(getString(R.string.cancel)){ _, _ ->}
        builder.setView(picker)
        return builder.create()
    }

    fun setValueChangeListener(onValueChangeListener: OnValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener
    }
}