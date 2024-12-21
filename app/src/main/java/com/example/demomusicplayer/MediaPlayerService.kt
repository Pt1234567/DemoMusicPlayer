package com.example.demomusicplayer

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.session.MediaButtonReceiver

class MediaPlayerService : Service() {

    private lateinit var player: MediaPlayer
    private val binder = MediaPlayerBinder()
    private val songList = listOf(
        R.raw.audio1, // Replace with actual song files in raw folder
        R.raw.audio // Add more songs if needed
    )
    private var currentSongIndex = 0

    companion object {
        lateinit var mediaSession: MediaSessionCompat
    }
    private fun requestAudioFocus(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager.requestAudioFocus(
            AudioManager.OnAudioFocusChangeListener { focusChange ->
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    pauseMusic()
                }
            },
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }
    override fun onCreate() {
        super.onCreate()

        // Initialize MediaPlayer
        player = MediaPlayer().apply {
            setOnPreparedListener { start() }
            setOnCompletionListener { skipToNext() }
        }

        // Initialize MediaSessionCompat
        mediaSession = MediaSessionCompat(this, "MediaPlayerService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    if (requestAudioFocus()) {
                        playMusic()
                        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                        isActive = true
                    }
                }

                override fun onPause() {
                    pauseMusic()
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                }

                override fun onSkipToNext() {
                    skipToNext()
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }

                override fun onSkipToPrevious() {
                    skipToPrevious()
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }

                override fun onStop() {
                    stopMusic()
                    updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
                }
            })

            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
            val pendingIntent = PendingIntent.getBroadcast(
                this@MediaPlayerService, 0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE
            )
            setMediaButtonReceiver(pendingIntent)

            isActive = true
        }

        // Register Media Button Event Receiver
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.registerMediaButtonEventReceiver(
            PendingIntent.getBroadcast(
                this, 0, Intent(Intent.ACTION_MEDIA_BUTTON), PendingIntent.FLAG_IMMUTABLE
            )
        )

        // Load the first song
        loadAudioFromRaw(songList[currentSongIndex])
    }

    private fun updatePlaybackState(state: Int) {
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_STOP
            )
            .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
            .build()
        mediaSession.setPlaybackState(playbackState)
    }

    private fun loadAudioFromRaw(resourceId: Int) {
        try {
            if (player.isPlaying) {
                player.stop()
            }
            player.reset()
            val afd = resources.openRawResourceFd(resourceId)
            player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            player.prepareAsync()
        } catch (e: Exception) {
            Log.e("MediaPlayerService", "Error loading audio: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { MediaButtonReceiver.handleIntent(mediaSession, it) }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun playMusic() {
        if (!player.isPlaying) {
            player.start()
        }
    }

    fun pauseMusic() {
        if (player.isPlaying) {
            player.pause()
        }
    }

    fun stopMusic() {
        if (player.isPlaying) {
            player.stop()
        }
    }

    fun skipToNext() {
        currentSongIndex = (currentSongIndex + 1) % songList.size
        loadAudioFromRaw(songList[currentSongIndex])
    }

    fun skipToPrevious() {
        currentSongIndex = if (currentSongIndex - 1 < 0) songList.size - 1 else currentSongIndex - 1
        loadAudioFromRaw(songList[currentSongIndex])
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        mediaSession.release()
    }

    inner class MediaPlayerBinder : Binder() {
        fun getService(): MediaPlayerService = this@MediaPlayerService
    }
}
