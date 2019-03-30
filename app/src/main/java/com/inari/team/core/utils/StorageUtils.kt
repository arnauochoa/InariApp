package com.inari.team.core.utils

import android.annotation.SuppressLint
import android.os.Environment
import okhttp3.MediaType
import okhttp3.ResponseBody
import java.io.*

val root: File =
    android.os.Environment.getExternalStorageDirectory()

const val APP_ROOT: String = "/Inari/Logs/"

const val POSITION_ROOT: String = "/Inari/Positions/"


@SuppressLint("SetWorldReadable")
fun saveFile(
    url: String,
    responseBody: ResponseBody
) {
    try {

        val dir =
            File(root.absolutePath + APP_ROOT)
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

//fun saveFile() {
//    try {
//
//        file.setReadable(true, false)
//
//        var inputStream: InputStream? = null
//        var outputStream: OutputStream? = null
//
//        try {
//            val fileReader = ByteArray(4096)
//
//            inputStream = file.readBytes().inputStream()
//            outputStream = FileOutputStream(file)
//
//            while (true) {
//                val read = inputStream.read(fileReader)
//                if (read == -1) {
//                    break
//                }
//                outputStream.write(fileReader, 0, read)
//            }
//            outputStream.flush()
//
//        } catch (e: IOException) {
//        } finally {
//            inputStream?.close()
//            outputStream?.close()
//        }
//    } catch (e: IOException) {
//    }
//}

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

fun getFile(fileName: String): File = File(root.absolutePath + APP_ROOT + fileName)


fun createDirectory(directoryName: String) {
    val dir =
        File(root.absolutePath + APP_ROOT + directoryName)
    dir.mkdirs()
}

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

fun retrieveFile(url: String): String {
    return try {
        val file = ResponseBody.create(
            MediaType.parse("text/plain"),
            getStringFromFile("${root.absolutePath}$APP_ROOT$url")
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

fun createFile(fileName: String): File {
    return File(root.absolutePath + APP_ROOT + fileName)
}

fun writeToFile(url: String, text: String) {
    try {
        val fw = FileWriter(File(root.absolutePath + APP_ROOT + url))
        fw.write(text)
        fw.close()
    } catch (e: Exception) {

    }
}

