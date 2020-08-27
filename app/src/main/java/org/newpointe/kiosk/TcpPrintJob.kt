package org.newpointe.kiosk

import org.newpointe.kiosk.models.CheckinLabel
import java.net.Socket
import java.net.URL

class TcpPrintJob(private val labels: Array<CheckinLabel>) : Thread() {
    override fun run() {
        for (label in labels) {
            val (address, port) = parseAddress(label.PrinterAddress)
            val labelContent = getLabelContent(label.LabelFile)
            val mergedContent = mergeLabel(labelContent, label.MergeFields)
            print(address, port, mergedContent)
        }
    }

    private fun parseAddress(address: String, defaultPort: Int = 9100): Pair<String, Int> {
        val addressParts = address.split(':', limit = 1)
        return when {
            addressParts.isEmpty() -> Pair("", defaultPort)
            addressParts.size == 1 -> Pair(address, defaultPort)
            else -> Pair(addressParts[0], addressParts[1].toIntOrNull(10) ?: defaultPort)
        }
    }

    private fun getLabelContent(url: URL): String {
        return url.openStream().readToEnd()
    }

    private fun mergeLabel(content: String, mergeFields: HashMap<String, String>): String {
        var mergedContent = content
        for ((key, value) in mergeFields) {
            if (value.isNotEmpty()) {
                mergedContent = mergedContent.replace(Regex("(?<=\\^FD)($key)(?=\\^FS)"), value)
            } else {
                mergedContent =
                    mergedContent.replace(Regex("\\^FO.*\\^FS\\s*(?=\\^FT.*\\^FD$key\\^FS)"), "")
                mergedContent = mergedContent.replace(Regex("\\^FD$key\\^FS"), "")
            }
        }
        return mergedContent
    }

    private fun print(address: String, port: Int, data: String) {
        val client = Socket(address, port)
        client.outputStream.write(data.toByteArray())
        client.close()
    }
}