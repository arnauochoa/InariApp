package com.inari.team.core.utils

import android.content.Context
import android.content.Intent
import android.support.v4.content.FileProvider
import java.io.File

fun shareTextFile(filename: String, context: Context) {
    val data = FileProvider.getUriForFile(context, "com.inari.team.provider", File(filename))
    context.grantUriPermission(context.packageName, data, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val intent = Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_STREAM, data)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, "Share log"))
}

fun openFileIntent(filename: String, context: Context) {
    val data = FileProvider.getUriForFile(context, "com.inari.team.provider", File(filename))
    context.grantUriPermission(context.packageName, data, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(data, "text/plain")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(intent)
}