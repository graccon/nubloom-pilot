package com.sujin.nubloompilot.receivers

import android.content.BroadcastReceiver

import android.content.Context

import android.content.Intent

import android.util.Log

import android.widget.Toast

class UserPresentReceiver : BroadcastReceiver() {

    override fun onReceive(

        context: Context,

        intent: Intent

    ) {

        Log.d("SleepWakeTrigger", "onReceive called: ${intent.action}")

        if (intent.action == Intent.ACTION_USER_PRESENT) {

            Log.d("SleepWakeTrigger", "사용자가 잠금해제했습니다.")

            Toast.makeText(

                context,

                "잠금해제 감지",

                Toast.LENGTH_SHORT

            ).show()

        }

    }
}