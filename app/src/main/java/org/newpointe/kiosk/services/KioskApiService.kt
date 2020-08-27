package org.newpointe.kiosk.services

import android.content.Context
import android.content.Intent
import info.laht.yajrpc.RpcService
import info.laht.yajrpc.RpcMethod
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.newpointe.kiosk.activities.SettingsActivity
import org.newpointe.kiosk.models.CheckinLabel
import org.newpointe.kiosk.models.ZebraCard

@Suppress("FunctionName")
internal class KioskApiService(
    private val context: Context,
    private val settingsService: SettingsService,
    private val labelPrintService: LabelPrintService,
    private val cardPrintService: CardPrintService,
    private val cameraService: CameraService
) : RpcService {

    override val serviceName get() = this.javaClass.name

    @RpcMethod
    fun PrintLabels(labels: Array<CheckinLabel>) {
        GlobalScope.launch { labelPrintService.printLabels(labels) }
    }

    @RpcMethod
    fun StartCamera(passive: Boolean) {
        cameraService.start(passive)
    }

    @RpcMethod
    fun StopCamera() {
        cameraService.stop()
    }

    @RpcMethod
    fun SetKioskId(kioskId: Number) {
        cameraService.setKioskId(kioskId)
    }

    @RpcMethod
    fun PrintCards(cards: Array<ZebraCard>) {
        cardPrintService.print(cards)
    }

    @RpcMethod
    fun GetAppPreference(key: String): String? {
        return settingsService.getString(key)
    }


    @RpcMethod
    fun SetAppPreference(key: String, value: String?): Boolean {
        settingsService.setString(key, value)
        return true
    }

    @RpcMethod
    fun ShowSettings() {
        context.startActivity(Intent(context, SettingsActivity::class.java))
    }
}