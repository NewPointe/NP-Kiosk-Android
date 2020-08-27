package org.newpointe.kiosk.services

import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.comm.TcpConnection
import org.newpointe.kiosk.models.CachedLabelData
import org.newpointe.kiosk.models.CheckinLabel
import org.newpointe.kiosk.models.PrintJob
import java.net.URI
import java.net.URL

fun createConnection(address: URI): Connection? {
    return when (address.scheme) {
        "tcp" -> TcpConnection(address.host, address.port)
        "bluetooth" -> BluetoothConnection(address.host)
        else -> null
    }
}

class LabelPrintService(
    private val settingsService: SettingsService
    private val labelCacheService: LabelCacheService
) {

    private fun normalizePrinterAddress(printerAddress: String): URI? {
        val parts = printerAddress.split(':', limit = 2).filter { it.isNotEmpty() }
        return when (parts.size) {
            2 -> {
                when (parts[0]) {
                    "bluetooth" -> URI("bluetooth", parts[1].trim('[', ']', '/'), "", "")
                    "usb" -> URI("usb", parts[1].trim('[', ']', '/'), "", "")
                    "tcp" -> URI("tcp", "//" + parts[1].trim('/'), "")
                    else -> URI("tcp", "//" + printerAddress.trim('/'), "")
                }
            }
            1 -> {
                URI("tcp", "//" + printerAddress.trim('/'), "")
            }
            else -> {
                null
            }
        }
    }

    fun print(labels: Array<CheckinLabel>): List<Error> {
        val printerOverride = this.settingsService.getPrinterOverride()
        val jobs = mutableListOf<PrintJob>()
        val errors = mutableListOf<Error>()
        val seenLabels = mutableMapOf<URL, CachedLabelData>()

        // Create the print job for each label
        for (label in labels) {

            val printerAddress = printerOverride ?: label.PrinterAddress
            if(printerAddress.isNullOrBlank()) {
                errors.add(Error("Could not print label: Invalid printer: Printer Address is null"))
                continue
            }

            val printerUri = normalizePrinterAddress(printerAddress)
            if(printerUri == null) {
                errors.add(Error("Could not print label: Invalid printer: Printer Address is null"))
                continue
            }

            val labelUrl = label.LabelFile
            if(labelUrl == null) {
                errors.add(Error("Could not print label: Invalid label URL"))
                continue
            }

            val labelData = this.labelCacheService.getLabel(labelUrl)
            val printData = labelData.getMerged(label.MergeFields)
            jobs.add(RawPrintJob(printerUri, printData))

        }
        return printLabels(jobs)
    }

    private fun print(jobs: Array<RawPrintJob>) {

    }

    private fun printLabels(jobs: List<PrintJob>): List<Error> {
        val seenLabels = mutableMapOf<URL, CachedLabelData>()
        val errors = mutableListOf<Error>()
        val jobsByConnection = jobs.groupBy { it.printerUri }
        for (jobGroup in jobsByConnection) {
            val connection = createConnection(jobGroup.key)
            if(connection != null) {
                try {
                    // Open the connection - physical connection is established here.
                    connection.open()

                    for (job in jobGroup.value) {

                        val labelData = seenLabels[job.labelURL] ?: getLabel(job.labelURL).also { seenLabels[job.labelURL] = it }

                        val zplData = mergeLabelData(labelData.labelData, job.mergeFields)

                        // Send the data to printer as a byte array.
                        connection.write(zplData.toByteArray())
                    }
                } catch (e: ConnectionException) {
                    // Handle communications error here.
                    e.printStackTrace()
                } finally {
                    // Close the connection to release resources.
                    connection.close()
                }
            }
        }
        return errors
    }
}


/*

Android:
    Labels:
        TCP
        USB
        Bluetooth
    Cards:
        TCP
        USB
iOS:
    Labels:
        TCP
        Bluetooth


tcp://host:port
tcp://ipv4:port
tcp://[ipv6]:port
usb://device_path
bluetooth://device_mac

*/