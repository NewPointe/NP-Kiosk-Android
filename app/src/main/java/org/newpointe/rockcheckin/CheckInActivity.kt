package org.newpointe.kiosk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import info.laht.yajrpc.RpcHandler
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

/**
 * The main Check-in activity. It displays the configured check-in website
 * in a full-screen web view and injects an API for label printing.
 */
class CheckInActivity : AppCompatActivity() {

    /**
     * The WebView
     */
    private lateinit var webView: WebView

    /**
     * The client api
     */
    private val clientApi = ClientApiService(this)

    /**
     * The rpc handler
     */
    private val rpcHandler = RpcHandler(clientApi)

    /**
     * The client js
     */
    private var clientJs = ""

    /**
     * If the settings are being shown already. Used to debounce the settings touch trigger.
     */
    private var settingsShowing = false

    /**
     * Run when the activity is created
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this).also {
            it.settings.javaScriptEnabled = true
            it.setOnTouchListener(this::onWebViewTouched)
            it.settings.userAgentString += " iPad"
        }
        setContentView(webView)
        clientJs = this.resources.openRawResource(R.raw.messagingclient).readToEnd()
    }

    /**
     * Run when the activity is resumed
     */
    override fun onResume() {
        super.onResume()
        settingsShowing = false
        setupCheckin()
    }

    /**
     * Sets up the WebView with the configured checkin address
     */
    private fun setupCheckin() {

        // Get the configured check-in address
        val address = clientApi.getAppPreference("checkin_address")

        // Check if the check-in address is set
        if (address.isNullOrEmpty()) {
            this.startActivity(Intent(this, FirstTimeSetupActivity::class.java))
        }
        else {

            // Parse the address
            val addressUri = Uri.parse(address)

            // Create a new rpc server
            val rpcServer = RpcWebMessageServer(rpcHandler, webView, addressUri)

            // Setup the WebViewClient to handle navigation and messaging
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    webView.evaluateJavascript(clientJs) {
                        rpcServer.start()
                    }
                }
            }

            // Load the url
            webView.loadUrl(address)
        }
    }

    /**
     * Run when the WebView is touched
     */
    @Suppress("UNUSED_PARAMETER")
    private fun onWebViewTouched(v: View?, event: MotionEvent?): Boolean {
        if(!settingsShowing && event?.pointerCount == 5) {
            settingsShowing = true
            clientApi.showSettings()
        }
        return settingsShowing
    }

}