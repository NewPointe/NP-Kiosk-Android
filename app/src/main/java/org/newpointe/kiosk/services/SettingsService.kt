package org.newpointe.kiosk.services

import android.content.Context
import android.content.SharedPreferences
import android.webkit.URLUtil
import androidx.preference.PreferenceManager
import org.newpointe.kiosk.R

class SettingsService(private val context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private fun getString(resId: Int): String? {
        return sharedPreferences.getString(context.getString(resId), null)
    }

    private fun getBoolean(resourceId: Int, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(context.getString(resourceId), defaultValue)
    }

    private fun getInt(resourceId: Int, defaultValue: Int): Int{
        return sharedPreferences.getInt(context.getString(resourceId), defaultValue)
    }

    private fun setString(resId: Int, value: String?) {
        sharedPreferences.edit().apply {
            putString(context.getString(resId), value)
            apply()
        }
    }

    private fun setBoolean(resId: Int, value: Boolean?) {
        sharedPreferences.edit().apply {
            val key = context.getString(resId)
            if(value == null) remove(key)
            else putBoolean(key, value)
            apply()
        }
    }

    private fun setInt(resId: Int, value: Int?) {
        sharedPreferences.edit().apply {
            val key = context.getString(resId)
            if(value == null) remove(key)
            else putInt(key, value)
            apply()
        }
    }

    fun getKioskAddress(): String? {
        return getString(R.string.preference_kiosk_address) ?: getString(R.string.preference_kiosk_address_alt)
    }

    fun setKioskAddress(value: String?) {
        setString(R.string.preference_kiosk_address_alt, null)
        setString(R.string.preference_kiosk_address, value)
    }

    fun getInAppSettingsEnabled(): Boolean {
        return getBoolean(R.string.preference_in_app_settings, false)
    }

    fun setInAppSettingsEnabled(value: Boolean?) {
        return setBoolean(R.string.preference_in_app_settings, value)
    }

    fun getInAppSettingsDelay(): Int {
        return getInt(R.string.preference_in_app_settings_delay, 0)
    }

    fun setInAppSettingsDelay(value: Int?) {
        return setInt(R.string.preference_in_app_settings_delay, value)
    }

    fun getLabelCachingEnabled(): Boolean {
        return getBoolean(R.string.preference_enable_caching, false)
    }

    fun setLabelCachingEnabled(value: Boolean?) {
        return setBoolean(R.string.preference_enable_caching, value)
    }

    fun getLabelCachingDuration(): Int {
        return getInt(R.string.preference_cache_duration, 0)
    }

    fun setLabelCachingDuration(value: Int?) {
        return setInt(R.string.preference_cache_duration, value)
    }

    fun getPrinterOverride(): String? {
        return getString(R.string.preference_printer_override)
    }

    fun setPrinterOverride(value: String?) {
        setString(R.string.preference_printer_override, value)
    }
}