package com.example.demomusicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.media.session.MediaButtonReceiver

class MediaButtonIntentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MediaButtonReceiver", "Media button pressed: ${intent.action}")
        // Handle media button intent with the MediaSession
        MediaButtonReceiver.handleIntent(MainActivity().mediaSession, intent)
    }
}
