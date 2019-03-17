package com.inari.team.utils

import android.annotation.SuppressLint
import android.os.Environment
import okhttp3.MediaType
import okhttp3.ResponseBody
import java.io.*

private val root: File =
        android.os.Environment.getExternalStorageDirectory()

private const val APP_ROOT: String = "/Inari/Logs/"


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

fun createDirectory (directoryName: String){
    val dir =
        File(root.absolutePath + APP_ROOT + directoryName)
    dir.mkdirs()
}

fun getFilesList(): Array<File> {
    val path = Environment.getExternalStorageDirectory().toString() + APP_ROOT
    val directory = File(path)
    return directory.listFiles()
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

