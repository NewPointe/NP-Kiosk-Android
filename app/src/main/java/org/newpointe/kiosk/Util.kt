package org.newpointe.kiosk

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

fun InputStream.readToEnd(): String {
    val outputStream = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var length: Int
    while (this.read(buffer).also { length = it } != -1) {
        outputStream.write(buffer, 0, length)
    }
    return outputStream.toString(StandardCharsets.UTF_8.name())
}
