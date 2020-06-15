package org.newpointe.kiosk

import android.net.Uri
import android.util.Log
import android.webkit.WebMessage
import android.webkit.WebMessagePort
import android.webkit.WebView
import info.laht.yajrpc.RpcHandler
import info.laht.yajrpc.net.RpcServer

const val CHECKIN_API_INIT: String = "org.newpointe.kiosk.CHECKIN_API_INIT"

open class RpcWebMessageServer(
    private val handler: RpcHandler,
    private val webView: WebView,
    private val allowedOrigin: Uri
) : RpcServer {
    override var port: Int? = null
    private var serverMessagingPort: WebMessagePort? = null
    private var clientMessagingPort: WebMessagePort? = null

    override fun start(port: Int) {
        // Make sure everything is stopped and cleaned up
        stop()

        // Create new messaging ports
        val (serverPort, clientPort) = webView.createWebMessageChannel()

        // Hook up the server port message callback
        serverPort.setWebMessageCallback(object : WebMessagePort.WebMessageCallback() {
            override fun onMessage(port: WebMessagePort?, message: WebMessage?) {
                if (message != null) {
                    try {
                        val result = handler.handle(message.data)
                        serverMessagingPort?.postMessage(WebMessage(result))
                    } catch (e: com.google.gson.JsonSyntaxException) {
                        Log.e(this.javaClass.name, "Error reading message", e)
                    }
                }
            }
        })

        // Send the client port to the page
        webView.postWebMessage(WebMessage(CHECKIN_API_INIT, arrayOf(clientPort)), allowedOrigin)

        // Save the messaging ports
        this.serverMessagingPort = serverPort
        this.clientMessagingPort = clientPort
    }

    override fun stop() {
        this.serverMessagingPort?.close()
        this.serverMessagingPort = null
        this.clientMessagingPort = null
    }

    override fun getAvailablePort(): Int {
        return -1
    }
}