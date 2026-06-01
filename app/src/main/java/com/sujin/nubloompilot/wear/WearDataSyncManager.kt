package com.sujin.nubloompilot.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.sujin.nubloompilot.shared.models.WatchTimelinePayload
import kotlinx.coroutines.tasks.await

class WearDataSyncManager(
    private val context: Context
) {
    private val dataClient by lazy { Wearable.getDataClient(context) }

    suspend fun syncTimelinePayload(
        payload: WatchTimelinePayload
    ): Result<Unit> {
        return try {
            val request = PutDataMapRequest.create("/nubloom/timeline").apply {
                dataMap.apply {
                    payload.yesterdayShift?.let { putString("yesterdayShift", it) }
                    payload.todayShift?.let { putString("todayShift", it) }
                    payload.tomorrowShift?.let { putString("tomorrowShift", it) }
                    payload.dayAfterTomorrowShift?.let { putString("dayAfterTomorrowShift", it) }
                    putString("referenceDate", payload.referenceDate)
                    putLong("updatedAt", payload.updatedAt)

                    val interventionMaps = ArrayList<DataMap>()
                    payload.interventions.forEach { intervention ->
                        val map = DataMap().apply {
                            putString("type", intervention.type)
                            putString("actionType", intervention.actionType)
                            putString("startTime", intervention.startTime)
                            putString("endTime", intervention.endTime)
                            putString("title", intervention.title)
                            putString("description", intervention.description)
                        }
                        interventionMaps.add(map)
                    }
                    putDataMapArrayList("interventions", interventionMaps)
                }
            }

            val putDataRequest = request.asPutDataRequest().setUrgent()
            dataClient.putDataItem(putDataRequest).await()

            Log.d(TAG, "Timeline payload synced")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync timeline payload", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "WearDataSyncManager"
    }
}
