package com.inari.team.core.utils

import android.app.AlertDialog
import android.content.Context

fun showAlert(
    context: Context,
    errorTitle: String,
    errorMessage: String,
    textPositive: String,
    positiveAction: () -> Unit,
    isCancelable: Boolean,
    textNegative: String = "",
    negativeAction: () -> Unit = {}
) {
    val builder = AlertDialog.Builder(context)
    with(builder) {
        setTitle(errorTitle)
        setMessage(errorMessage)
        setPositiveButton(textPositive) { _, _ ->
            positiveAction.invoke()
        }
        if (textNegative.isNotBlank()) {
            setNegativeButton(textNegative) { _, _ ->
                negativeAction.invoke()
            }
        }
        if (isCancelable) {
            setNeutralButton("Cancel") { _, _ ->
            }
        }
        setCancelable(isCancelable)

        show()
    }
}

