package org.newpointe.kiosk.models

import java.net.URL

@Suppress("PropertyName")
class CheckinLabel {
    var LabelFile: URL? = null
    var MergeFields: HashMap<String, String>? = null
    var PrinterAddress: String? = null
}