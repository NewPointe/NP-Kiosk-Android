package org.newpointe.kiosk.models

import java.net.URL

class Label {
    var FileGuid: String = ""
    lateinit var LabelFile: URL
    var LabelKey: String = ""
    var LabelType: Int = 0
    lateinit var MergeFields: HashMap<String, String>
    var Order: Int = 0
    var PersonId: Int = 0
    var PrintFrom: Int = 0
    var PrintTo: Int = 0
    var PrinterAddress: String = ""
    var PrinterDeviceId: Int = 0
}