package org.newpointe.kiosk.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.newpointe.kiosk.models.CachedLabelData
import org.newpointe.kiosk.readToEnd
import java.net.URL

class LabelCacheService(
    private val settingsService: SettingsService
) {
    private val labelCache = mutableMapOf<URL, CachedLabelData>()

    fun getCachedLabel(labelUrl: URL): CachedLabelData? {
        val existingLabel = labelCache[labelUrl]
        return if (existingLabel != null && existingLabel.getAge() < settingsService.getLabelCachingDuration()) {
            existingLabel
        } else {
            null
        }
    }

    suspend fun getRemoteLabel(labelUrl: URL): CachedLabelData =
        withContext(Dispatchers.IO) {
            CachedLabelData(labelUrl, labelUrl.openStream().readToEnd())
        }

    suspend fun getLabel(labelUrl: URL): CachedLabelData {
        return if (settingsService.getLabelCachingEnabled()) {
            val cachedLabel = getCachedLabel(labelUrl)
            if (cachedLabel != null) {
                cachedLabel
            } else {
                val newLabel = getRemoteLabel(labelUrl)
                labelCache[labelUrl] = newLabel
                newLabel
            }
        } else {
            getRemoteLabel(labelUrl)
        }
    }
}