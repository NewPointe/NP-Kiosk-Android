package org.newpointe.kiosk

import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import info.laht.yajrpc.RpcService
import info.laht.yajrpc.RpcMethod

internal class ClientApiService(
    private val context: Context
) : RpcService {

    override val serviceName get() = this.javaClass.name

    @RpcMethod
    fun print(input: Array<Label>) {
        TcpPrintJob(input).start()
    }

    @RpcMethod
    fun getAppPreference(key: String): String? {
        return PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(key, "")
    }


    @RpcMethod
    fun setAppPreference(key: String, value: String?): Boolean {
        return PreferenceManager
            .getDefaultSharedPreferences(context)
            .edit()
            .run {
                putString(key, value)
                commit()
            }
    }

    @RpcMethod
    fun showSettings() {
        context.startActivity(Intent(context, SettingsActivity::class.java))
    }
}