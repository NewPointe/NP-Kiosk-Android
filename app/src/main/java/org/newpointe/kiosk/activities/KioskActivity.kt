package org.newpointe.kiosk.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

import info.laht.yajrpc.RpcHandler

import org.newpointe.kiosk.R
import org.newpointe.kiosk.RpcWebMessageServer
import org.newpointe.kiosk.readToEnd
import org.newpointe.kiosk.services.KioskApiService
import org.newpointe.kiosk.services.SettingsService


/**
 * The main Check-in activity. It displays the configured check-in website
 * in a full-screen web view and injects an API for label printing.
 */
class KioskActivity : AppCompatActivity() {

    /**
     * The settings service
     */
    private lateinit var preferences: SettingsService

    /**
     * The WebView
     */
    private lateinit var webView: WebView

    /**
     * The client api
     */
    private val clientApi = KioskApiService(this)

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
        preferences = SettingsService(this)
        webView = WebView(this).also {
            it.settings.javaScriptEnabled = true
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
        val address = preferences.getKioskAddress() ?: ""

        // Check if the check-in address is set
        if (address.isNullOrEmpty()) {
            this.startActivity(Intent(this, FirstTimeSetupActivity::class.java))
            finish()
        } else {

            // Parse the address
            val addressUri = Uri.parse(address)

            // Create a new rpc server
            val rpcServer = RpcWebMessageServer(rpcHandler, webView, addressUri)

            // Setup the WebViewClient to handle navigation and messaging
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    webView.evaluateJavascript(clientJs) {
                        rpcServer.start(0)
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
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (!settingsShowing && event?.pointerCount == 5) {
            settingsShowing = true
            clientApi.ShowSettings()
            return true
        }
        return super.dispatchTouchEvent(event)
    }

}