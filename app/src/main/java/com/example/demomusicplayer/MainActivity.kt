package com.example.demomusicplayer

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

class MainActivity : AppCompatActivity() {

     lateinit var mediaSession: MediaSessionCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playButton: Button = findViewById(R.id.playButton)
        val pauseButton: Button = findViewById(R.id.pauseButton)
        val nextButton: Button = findViewById(R.id.nextButton)
        val prevButton: Button = findViewById(R.id.prevButton)

        setupMediaSession()

        playButton.setOnClickListener { onPlayPressed() }
        pauseButton.setOnClickListener { onPausePressed() }
        nextButton.setOnClickListener { onNextPressed() }
        prevButton.setOnClickListener { onPreviousPressed() }
    }

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, "MediaSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    onPlayPressed()
                }

                override fun onPause() {
                    onPausePressed()
                }

                override fun onSkipToNext() {
                    onNextPressed()
                }

                override fun onSkipToPrevious() {
                    onPreviousPressed()
                }

                override fun onStop() {
                    onStopPressed()
                }
            })

            val state = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_STOP
                )
                .setState(
                    PlaybackStateCompat.STATE_STOPPED,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                    0f
                )
                .build()

            setPlaybackState(state)
            isActive = true
        }
    }

    private fun onPlayPressed() {
        Toast.makeText(this, "Play button pressed", Toast.LENGTH_SHORT).show()
        Log.d("MediaSession", "Play button pressed")
    }

    private fun onPausePressed() {
        Toast.makeText(this, "Pause button pressed", Toast.LENGTH_SHORT).show()
        Log.d("MediaSession", "Pause button pressed")
    }

    private fun onNextPressed() {
        Toast.makeText(this, "Next button pressed", Toast.LENGTH_SHORT).show()
        Log.d("MediaSession", "Next button pressed")
    }

    private fun onPreviousPressed() {
        Toast.makeText(this, "Previous button pressed", Toast.LENGTH_SHORT).show()
        Log.d("MediaSession", "Previous button pressed")
    }

    private fun onStopPressed() {
        Toast.makeText(this, "Stop button pressed", Toast.LENGTH_SHORT).show()
        Log.d("MediaSession", "Stop button pressed")
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
    }
}
