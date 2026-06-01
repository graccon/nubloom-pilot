package com.sujin.nubloompilot.wear

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class WearTimelineDataListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == "/nubloom/timeline") {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    
                    val yesterdayShift = dataMap.getString("yesterdayShift")
                    val todayShift = dataMap.getString("todayShift")
                    val tomorrowShift = dataMap.getString("tomorrowShift")
                    val dayAfterTomorrowShift = dataMap.getString("dayAfterTomorrowShift")
                    val referenceDate = dataMap.getString("referenceDate")
                    val updatedAt = dataMap.getLong("updatedAt")
                    val interventions = dataMap.getDataMapArrayList("interventions")

                    Log.d(TAG, "Data changed at /nubloom/timeline")
                    Log.d(TAG, "yesterdayShift: $yesterdayShift")
                    Log.d(TAG, "todayShift: $todayShift")
                    Log.d(TAG, "tomorrowShift: $tomorrowShift")
                    Log.d(TAG, "dayAfterTomorrowShift: $dayAfterTomorrowShift")
                    Log.d(TAG, "referenceDate: $referenceDate")
                    Log.d(TAG, "updatedAt: $updatedAt")
                    Log.d(TAG, "interventions count: ${interventions?.size ?: 0}")

                    // Save to local store
                    val localStore = WearTimelineLocalStore(applicationContext)
                    localStore.saveTimelineInfo(
                        yesterdayShift = yesterdayShift,
                        todayShift = todayShift,
                        tomorrowShift = tomorrowShift,
                        dayAfterTomorrowShift = dayAfterTomorrowShift,
                        referenceDate = referenceDate,
                        updatedAt = updatedAt
                    )
                    Log.d(TAG, "Successfully saved timeline info to LocalStore")
                }
            }
        }
    }

    companion object {
        private const val TAG = "WearTimelineListener"
    }
}
