package org.newpointe.kiosk.preferences

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.EditTextPreference
import org.newpointe.kiosk.R


/**
 * A custom EditTextPreference.
 *  - Allows setting a validator
 *  - Adds a hint attribute (defaults to default value)
 *  - Uses the default value if the value is empty
 */
class NPEditTextPreference(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : EditTextPreference(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {
    private var mOnValidatePreference: ((newValue: String) -> String?)? = null

    private var mDefaultValue: Any? = null

    var hint: String? = null
        get() = field ?: mDefaultValue?.toString()

    init {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.NPEditTextPreference, defStyleAttr, defStyleRes
        )

        hint = TypedArrayUtils.getString(
            a, R.styleable.NPEditTextPreference_hint, R.styleable.NPEditTextPreference_hint
        )

        if (a.hasValue(R.styleable.NPEditTextPreference_defaultValue)) {
            mDefaultValue = onGetDefaultValue(a, R.styleable.NPEditTextPreference_defaultValue)
        }

        a.recycle()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(
        context,
        attrs,
        TypedArrayUtils.getAttr(
            context,
            androidx.preference.R.attr.editTextPreferenceStyle,
            android.R.attr.editTextPreferenceStyle
        )
    )

    constructor(context: Context) : this(context, null)


    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) { // No need to save instance state since it's persistent
            return superState
        }
        val myState =
            SavedState(superState)
        myState.mHint = hint
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state.javaClass != SavedState::class.java) { // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }
        val myState =
            state as SavedState
        super.onRestoreInstanceState(myState.superState)
        hint = myState.mHint
    }


    private class SavedState : BaseSavedState {
        var mHint: String? = null

        internal constructor(source: Parcel) : super(source) {
            mHint = source.readString()
        }

        internal constructor(superState: Parcelable?) : super(superState) {}

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeString(mHint)
        }

        companion object CREATOR: Parcelable.Creator<SavedState>{
            override fun createFromParcel(`in`: Parcel): SavedState? {
                return SavedState(`in`)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    internal fun getOnValidatePreference(): ((newValue: String) -> String?)? {
        return mOnValidatePreference
    }

    fun setOnValidatePreference(value: ((newValue: String) -> String?)?) {
        mOnValidatePreference = value
    }

    override fun setDefaultValue(defaultValue: Any) {
        super.setDefaultValue(defaultValue)
        mDefaultValue = defaultValue
    }

    override fun setText(text: String?) {
        if((text == null || text == "") && mDefaultValue !== null) {
            super.setText(mDefaultValue.toString())
        }
        else {
            super.setText(text)
        }
    }

}