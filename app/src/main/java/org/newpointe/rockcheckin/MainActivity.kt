package org.newpointe.rockcheckin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.preference.PreferenceManager

const val EXTRA_URL = "org.newpointe.rockcheckin.URL"
const val EXTRA_PRINTER = "org.newpointe.rockcheckin.PRINTER"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onResume() {
        super.onResume()

        // Get the app's shared preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Get the configured check-in address
        val address = sharedPreferences.getString("checkin_address", "")

        // Check if it's set
        if (!address.isNullOrEmpty()) {

            // Start the main check-in activity
            startActivity(Intent(this, CheckInActivity::class.java))

        } else {

            val messageText = findViewById<EditText>(R.id.checkinAddressText)
            messageText.setText(address)

        }
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

        // Start the main check-in activity
        startActivity(Intent(this, CheckInActivity::class.java))

    }

}
