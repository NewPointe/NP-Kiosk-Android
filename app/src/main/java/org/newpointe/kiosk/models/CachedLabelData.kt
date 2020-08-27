package org.newpointe.kiosk.models

import android.os.SystemClock
import java.net.URL

class CachedLabelData(val url: URL, val content: String) {
    private val createdMilliseconds = SystemClock.elapsedRealtime()

    fun getAge(): Int {
        return ((SystemClock.elapsedRealtime() - createdMilliseconds) / 1000).toInt()
    }

    fun getMerged(fields: Map<String, String>?): String {
        if (fields == null) return this.content
        var mergedContent = this.content
        for ((key, value) in fields) {
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
}