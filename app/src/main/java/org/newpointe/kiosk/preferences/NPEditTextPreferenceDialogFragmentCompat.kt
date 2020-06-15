package org.newpointe.kiosk.preferences

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.EditTextPreferenceDialogFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat


/**
 * A custom DialogFragment for an NPEditTextPreference.
 *  - Adds validation support
 *  - Makes hitting done on the keyboard automatically save instead of just dismissing the keyboard
 *  - Adds an EditText hint, using the default value if the hint isn't specified
 */
class NPEditTextPreferenceDialogFragmentCompat : EditTextPreferenceDialogFragmentCompat(),
    TextView.OnEditorActionListener,
    TextWatcher,
    Preference.OnPreferenceChangeListener {

    companion object {
        @JvmStatic
        fun newInstance(key: String): NPEditTextPreferenceDialogFragmentCompat {
            val fragment =
                NPEditTextPreferenceDialogFragmentCompat()
            val b = Bundle(1)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }

    private var npEditTextPreference: NPEditTextPreference? = null
    private var editText: EditText? = null
    private var positiveButton: Button? = null

    override fun onStart() {
        super.onStart()

        positiveButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton?.isEnabled = validate() == null
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        npEditTextPreference = preference as NPEditTextPreference
        editText = view.findViewById(android.R.id.edit)

        val editText = checkNotNull(editText) {
            "Dialog view must contain an EditText with id @android:id/edit"
        }

        editText.imeOptions = EditorInfo.IME_ACTION_DONE

        npEditTextPreference?.hint?.also { editText.hint = it }

        editText.setOnEditorActionListener(this)
        editText.addTextChangedListener(this)
        npEditTextPreference?.onPreferenceChangeListener = this
    }

    private fun validate(): String? {
        return editText?.let { validate(it.text.toString()) }
    }

    private fun validate(newValue: String): String? {
        return npEditTextPreference?.getOnValidatePreference()?.let { it(newValue) }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        // Run validator
        // If valid, enable save button & clear error
        // If invalid, disable save button

        val message = validate()
        if (message == null) {
            positiveButton?.isEnabled = true
            editText?.error = null
        } else {
            positiveButton?.isEnabled = false
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {

        // If done
        // Run validator
        // If valid, enable save button & clear error & click save button
        // If invalid, disable save button & show error

        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_SEND) {
            val message = validate()
            if (message == null) {
                positiveButton?.isEnabled = true
                editText?.error = null
                positiveButton?.performClick()
            } else {
                positiveButton?.isEnabled = false
                editText?.error = message
            }
            return true
        }
        return false
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return validate(newValue.toString()) == null
    }

}