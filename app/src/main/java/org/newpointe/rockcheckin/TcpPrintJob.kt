package org.newpointe.rockcheckin

import java.net.Socket

class TcpPrintJob(private val address: String, private val port: Int, private val data: String) : Thread() {
    override fun run() {
        val client = Socket(address, port)
        client.outputStream.write(data.toByteArray())
        client.close()
    }
}