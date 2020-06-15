package org.newpointe.kiosk.preferences

import androidx.fragment.app.DialogFragment
import androidx.preference.*

abstract class NPPreferenceFragmentCompat : PreferenceFragmentCompat() {

    companion object {
        private const val DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG"
    }

    @Throws(IllegalArgumentException::class)
    override fun onDisplayPreferenceDialog(preference: Preference) {

        // check if dialog is already showing
        if (fragmentManager!!.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return
        }

        val key = preference.key
        val f: DialogFragment = when (preference) {
            is NPEditTextPreference -> NPEditTextPreferenceDialogFragmentCompat.newInstance(key)
            else -> return super.onDisplayPreferenceDialog(preference)
        }

        f.setTargetFragment(this, 0)
        f.show(fragmentManager!!, DIALOG_FRAGMENT_TAG)
    }

}