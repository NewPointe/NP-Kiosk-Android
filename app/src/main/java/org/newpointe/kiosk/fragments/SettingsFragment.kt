package org.newpointe.kiosk.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.webkit.URLUtil
import org.newpointe.kiosk.R
import org.newpointe.kiosk.preferences.*

class SettingsFragment : NPPreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        // Inflate preferences from xml
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Add validation
        findPreference<NPEditTextPreference>(getString(R.string.preference_kiosk_address))?.setOnValidatePreference {
            when {
                it == "" -> getString(R.string.error_preference_kiosk_address_blank)
                URLUtil.isHttpUrl(it) -> getString(R.string.error_preference_kiosk_address_http)
                !URLUtil.isHttpsUrl(it) -> getString(R.string.error_preference_kiosk_address_invalid)
                else -> null
            }
        }

        findPreference<NPEditTextPreference>(getString(R.string.preference_in_app_settings_delay))?.setOnValidatePreference { text ->
            when {
                !TextUtils.isDigitsOnly(text) -> getString(R.string.error_preference_in_app_settings_delay_whole)
                else -> null
            }
        }

        findPreference<NPEditTextPreference>(getString(R.string.preference_cache_duration))?.setOnValidatePreference { text ->
            when {
                !TextUtils.isDigitsOnly(text) -> getString(R.string.error_preference_cache_duration_whole)
                else -> null
            }
        }


        findPreference<NPEditTextPreference>(getString(R.string.preference_printer_timeout))?.setOnValidatePreference { text ->
            when {
                !TextUtils.isDigitsOnly(text) -> getString(R.string.error_preference_printer_timeout_whole)
                else -> null
            }
        }


        findPreference<NPEditTextPreference>(getString(R.string.preference_ui_background_color))?.setOnValidatePreference { text ->
            if (text == "") null
            else try {
                Color.parseColor(text)
                null
            } catch (e: Exception) {
                getString(R.string.error_preference_ui_background_color_invalid)
            }
        }


        findPreference<NPEditTextPreference>(getString(R.string.preference_ui_foreground_color))?.setOnValidatePreference { text ->
            if (text == "") null
            else try {
                Color.parseColor(text)
                null
            } catch (e: Exception) {
                getString(R.string.error_preference_ui_foreground_color_invalid)
            }
        }

    }
}
