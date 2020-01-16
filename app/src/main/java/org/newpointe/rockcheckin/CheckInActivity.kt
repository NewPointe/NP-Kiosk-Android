package org.newpointe.rockcheckin

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.webkit.*
import androidx.preference.PreferenceManager

/**
 * The main Check-in activity. It displays the configured check-in website
 * in a full-screen web view and injects an API for label printing.
 */
class CheckInActivity : AppCompatActivity(), View.OnTouchListener {

    private var settingsShowing = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the WebView
        val webView = WebView(this)

        // Set the WebView as the activity's content
        setContentView(webView)

        // Get the app's shared preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Get the configured check-in address
        val address = sharedPreferences.getString("checkin_address", "")
        val addressUri = Uri.parse(address)


        if (address.isNullOrEmpty()) {
            startActivity(Intent(this, MySettingsActivity::class.java))
        }

        // Enable JavaScript
        webView.settings.javaScriptEnabled = true

        // Setup our WebViewClient to handle navigation and messaging
        webView.webViewClient = MyWebViewClient(addressUri)

        webView.setOnTouchListener(this)

        // Load the passed in url
        webView.loadUrl(address)

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if(!settingsShowing && event != null && event.pointerCount == 5) {
            settingsShowing = true
            startActivity(Intent(this, MySettingsActivity::class.java))
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        settingsShowing = false
    }

}

class MyWebViewClient(private val messagingOrigin: Uri): WebViewClient() {
    private val myWebMessageCallback = MyWebMessageCallback()
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if(view != null) {
            // Create a messaging channel
            var (hostPort, clientPort) = view.createWebMessageChannel()

            // Hook up our side of the messaging channel
            hostPort.setWebMessageCallback(myWebMessageCallback)

            // Set up the client side
            view.evaluateJavascript("console.log('Hi'); window.addEventListener(\"message\", (e) => { console.log(e); e.ports[0].postMessage(e.data);}, false);", MyValueCallback(view, clientPort, messagingOrigin))
        }
    }
}

class MyValueCallback(private val view: WebView, private val clientPort: WebMessagePort, private val messagingOrigin: Uri): ValueCallback<String> {
    override fun onReceiveValue(value: String?) {
        // Send an init message with the client's port
        val initMessage = WebMessage("CHECKIN_API_INIT", arrayOf(clientPort))
        view.postWebMessage(initMessage, messagingOrigin)
    }
}

class MyWebMessageCallback: WebMessagePort.WebMessageCallback() {
    override fun onMessage(port: WebMessagePort?, message: WebMessage?) {
        super.onMessage(port, message)
        Log.d(this.javaClass.name, message.toString())
    }
}