package com.inari.team.core.utils

import android.app.AlertDialog
import android.content.Context

fun showAlert(
    context: Context,
    errorTitle: String,
    errorMessage: String,
    textPositive: String,
    hasNegativeButton: Boolean,
    textNegative: String,
    hasCancelButton: Boolean,
    isCancelable: Boolean,
    positiveAction: () -> Unit,
    negativeAction: () -> Unit
) {
    val builder = AlertDialog.Builder(context)
    with(builder) {
        setTitle(errorTitle)
        setMessage(errorMessage)
        setPositiveButton(textPositive) { _, _ ->
            positiveAction.invoke()
        }
        if (hasNegativeButton) {
            setNegativeButton(textNegative) { _, _ ->
                negativeAction.invoke()
            }
        }
        if (hasCancelButton) {
            setNeutralButton("Cancel") { _, _ ->
            }
        }
        setCancelable(isCancelable)

        show()
    }
}

