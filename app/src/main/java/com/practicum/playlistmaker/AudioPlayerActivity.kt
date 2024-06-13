package com.practicum.playlistmaker

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {
    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val DELAY_MILLIS = 25L
        private const val TIME_FORMAT = "mm:ss"
        private const val ZERO_TIME = "00:00"

        fun startActivity(context: Context) {
            val intent = Intent(context, AudioPlayerActivity::class.java)
            context.startActivity(intent)
        }
    }

    private var playerState = STATE_DEFAULT
    private lateinit var playButton: ImageButton
    private lateinit var progressTimeView: TextView
    private var mediaPlayer = MediaPlayer()
    private var mainThreadHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        val searchHistory = SearchHistory(this)
        val name = findViewById<TextView>(R.id.title)
        val artist = findViewById<TextView>(R.id.artist)
        val duration = findViewById<TextView>(R.id.trackTime)
        val album = findViewById<TextView>(R.id.albumName)
        val year = findViewById<TextView>(R.id.year)
        val genre = findViewById<TextView>(R.id.styleName)
        val country = findViewById<TextView>(R.id.countryName)
        val artwork = findViewById<ImageView>(R.id.cover)
        playButton = findViewById(R.id.playButton)
        progressTimeView = findViewById(R.id.progressTime)

        mainThreadHandler = Handler(Looper.getMainLooper())
        val items = searchHistory.read()
        if (items.isNotEmpty()) {
            val item = items[0]
            name.text = item.trackName
            artist.text = item.artistName
            album.text = item.collectionName

            if (item.releaseDate.length >= 4) {
                year.text = item.releaseDate.substring(0, 4)
            }

            genre.text = item.primaryGenreName
            country.text = item.country
            duration.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(Date(item.trackTimeMillis))

            Glide.with(applicationContext)
                .load(item.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg"))
                .placeholder(R.drawable.icon_placeholder)
                .centerCrop()
                .into(artwork)
        }

        val imageBack = findViewById<ImageView>(R.id.backButton)
        imageBack.setOnClickListener { finish() }

        preparePlayer(items[0])
        playButton.setOnClickListener { playbackControl() }
    }

    private fun preparePlayer(item: Track) {
        mediaPlayer.setDataSource(item.previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playButton.isEnabled = true
            playerState = STATE_PREPARED
        }
        mediaPlayer.setOnCompletionListener {
            playerState = STATE_PREPARED
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        if (isDarkTheme()) {
            playButton.setImageResource(R.drawable.icon_pause_button_dark) // Используем иконку для тёмной темы
        } else {
            playButton.setImageResource(R.drawable.icon_pause_button) // Используем иконку для светлой темы
        }
        playerState = STATE_PLAYING
        mainThreadHandler?.post(createUpdateProgressTimeRunnable())
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        if (isDarkTheme()) {
            playButton.setImageResource(R.drawable.icon_play_button_dark) // Используем иконку для тёмной темы
        } else {
            playButton.setImageResource(R.drawable.icon_play_button) // Используем иконку для светлой темы
        }
        playerState = STATE_PAUSED
    }

    private fun isDarkTheme(): Boolean {
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
    }

    private fun createUpdateProgressTimeRunnable(): Runnable {
        return object : Runnable {
            override fun run() {
                when (playerState) {
                    STATE_PLAYING -> {
                        progressTimeView.text = SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).format(mediaPlayer.currentPosition)
                        mainThreadHandler?.postDelayed(this, DELAY_MILLIS)
                    }
                    STATE_PAUSED -> {
                        mainThreadHandler?.removeCallbacks(this)
                    }
                    STATE_PREPARED -> {
                        mainThreadHandler?.removeCallbacks(this)
                        playButton.setImageResource(R.drawable.icon_play_button)
                        progressTimeView.text = ZERO_TIME
                    }
                }
            }
        }
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED, STATE_PAUSED -> startPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
