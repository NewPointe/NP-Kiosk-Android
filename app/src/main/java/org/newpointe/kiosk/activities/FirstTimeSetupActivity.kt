package org.newpointe.kiosk.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText

import org.newpointe.kiosk.R
import org.newpointe.kiosk.services.SettingsService

class FirstTimeSetupActivity : AppCompatActivity() {

    private lateinit var preferences: SettingsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_time_setup)
        preferences = SettingsService(this)
    }

    override fun onResume() {
        super.onResume()

        // Get the configured check-in address
        val address = preferences.getKioskAddress() ?: ""

        val messageText = findViewById<EditText>(R.id.checkinAddressText)
        messageText.setText(address)

    }

    fun onSubmitButtonClicked(view: View) {

        // Set the configured check-in address
        val messageText = findViewById<EditText>(R.id.checkinAddressText)
        preferences.setKioskAddress(messageText.text.toString())

        // Go back to the main checkin activity
        startActivity(Intent(this, KioskActivity::class.java))
        finish()

    }

}
