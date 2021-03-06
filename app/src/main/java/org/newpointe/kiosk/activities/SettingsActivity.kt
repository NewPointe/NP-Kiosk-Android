package org.newpointe.kiosk.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import org.newpointe.kiosk.R
import org.newpointe.kiosk.fragments.SettingsFragment

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }
}
