package org.newpointe.kiosk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.preference.PreferenceManager

class FirstTimeSetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_time_setup)

    }

    override fun onResume() {
        super.onResume()

        // Get the app's shared preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Get the configured check-in address
        val address = sharedPreferences.getString("checkin_address", "")

        val messageText = findViewById<EditText>(R.id.checkinAddressText)
        messageText.setText(address)

    }

    fun onSubmitButtonClicked(view: View) {

        // Get the app's shared preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Set the configured check-in address
        val messageText = findViewById<EditText>(R.id.checkinAddressText)
        sharedPreferences.edit().apply {
            putString("checkin_address", messageText.text.toString())
            apply()
        }

        // Go back to the main checkin activity
        startActivity(Intent(this, CheckInActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP })

    }

}
