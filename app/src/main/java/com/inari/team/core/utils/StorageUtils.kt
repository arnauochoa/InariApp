package com.inari.team.core.utils

import android.annotation.SuppressLint
import android.os.Environment
import okhttp3.MediaType
import okhttp3.ResponseBody
import java.io.*

val root: File =
    android.os.Environment.getExternalStorageDirectory()

const val APP_ROOT: String = "/Inari-/Nmea/"

const val POSITION_ROOT: String = "/Inari/Positions/"

//used for post processing
//const val GNSS_ROOT: String = "/Inari/Gnss/"

@SuppressLint("SetWorldReadable")
fun savePositionFile(url: String, responseBody: ResponseBody) {
    try {

        val dir =
            File(root.absolutePath + POSITION_ROOT)
        dir.mkdirs()

        val file = File(dir, url)
        file.setReadable(true, false)

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            val fileReader = ByteArray(4096)

            inputStream = responseBody.byteStream()
            outputStream = FileOutputStream(file)

            while (true) {
                val read = inputStream!!.read(fileReader)

                if (read == -1) {
                    break
                }
                outputStream.write(fileReader, 0, read)
            }
            outputStream.flush()

        } catch (e: IOException) {
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    } catch (e: IOException) {
    }
}

//used for post processing
//@SuppressLint("SetWorldReadable")
//fun saveGnssFile(url: String, responseBody: ResponseBody) {
//    val dir =
//        File(root.absolutePath + GNSS_ROOT)
//    dir.mkdirs()
//
//    val file = File(dir, url)
//    file.setReadable(true, false)
//
//    var inputStream: InputStream? = null
//    var outputStream: OutputStream? = null
//
//    try {
//        val fileReader = ByteArray(4096)
//
//        inputStream = responseBody.byteStream()
//        outputStream = FileOutputStream(file)
//
//        while (true) {
//            val read = inputStream!!.read(fileReader)
//
//            if (read == -1) {
//                break
//            }
//            outputStream.write(fileReader, 0, read)
//        }
//        outputStream.flush()
//
//    } catch (e: IOException) {
//    } finally {
//        inputStream?.close()
//        outputStream?.close()
//    }
//}


fun getFile(fileName: String): File = File(root.absolutePath + APP_ROOT + fileName)

fun getFilesList(): Array<File> {
    val path = Environment.getExternalStorageDirectory().toString() + APP_ROOT
    val directory = File(path)

    return if (directory.exists()) {
        directory.listFiles()
    } else arrayOf()
}

fun getPositionsFilesList(): Array<File> {
    val path = Environment.getExternalStorageDirectory().toString() + POSITION_ROOT
    val directory = File(path)

    return if (directory.exists()) {
        directory.listFiles()
    } else arrayOf()
}

fun retrievePositionsFile(fileName: String): String {
    return try {
        val file = ResponseBody.create(
            MediaType.parse("text/plain"),
            getStringFromFile("${root.absolutePath}$POSITION_ROOT$fileName")
        ).string()
        file
    } catch (e: Exception) {
        ""
    }
}

fun deleteFile(fileName: String): Boolean {
    return try {
        val dir =
            File(root.absolutePath + APP_ROOT)
        dir.mkdirs()
        val file = File(dir, fileName)
        file.delete()
        true
    } catch (e: Exception) {
        false
    }
}

fun deletePositionFile(fileName: String): Boolean {
    return try {
        val dir =
            File(root.absolutePath + POSITION_ROOT)
        dir.mkdirs()
        val file = File(dir, fileName)
        file.delete()
        true
    } catch (e: Exception) {
        false
    }
}


