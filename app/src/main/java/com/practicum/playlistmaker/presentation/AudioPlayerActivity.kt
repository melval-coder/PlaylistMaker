package com.practicum.playlistmaker.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.domain.PlayerPresenter
import com.practicum.playlistmaker.domain.controllers.PlayControl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity(), PlayerPresenter {
    companion object {
        private const val DELAY_MILLIS = 25L

        fun startActivity(context: Context) {
            val intent = Intent(context, AudioPlayerActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var playControl: PlayControl
    private lateinit var playButton: ImageButton
    private lateinit var progressTimeView: TextView
    private var mainThreadHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        val name = findViewById<TextView>(R.id.title)
        val artist = findViewById<TextView>(R.id.artist)
        val duration = findViewById<TextView>(R.id.trackTime)
        val album = findViewById<TextView>(R.id.albumName)
        val year = findViewById<TextView>(R.id.year)
        val genre = findViewById<TextView>(R.id.styleName)
        val country = findViewById<TextView>(R.id.countryName)
        val artwork = findViewById<ImageView>(R.id.cover)
        val item = Creator.getOneTrackRepository(this).getTrack()
        playButton = findViewById(R.id.playButton)
        progressTimeView = findViewById(R.id.progressTime)
        playControl = Creator.createPlayControl(this)
        mainThreadHandler = Handler(Looper.getMainLooper())

        name.text = item.trackName
        artist.text = item.artistName
        album.text = item.collectionName
        year.text = item.releaseDate.substring(0, 4)
        genre.text = item.primaryGenreName
        country.text = item.country
        duration.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(Date(item.trackTimeMillis))
        playControl.preparePlayer(item)

        Glide.with(applicationContext)
            .load(item.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg"))
            .placeholder(R.drawable.icon_placeholder)
            .centerCrop()
            .into(artwork)

        val imageBack = findViewById<ImageView>(R.id.backButton)
        imageBack.setOnClickListener { finish() }

        playButton.setOnClickListener { playControl.playbackControl() }
    }

    override fun startPlayer() {
        if (isDarkTheme()) {
            playButton.setImageResource(R.drawable.icon_pause_button_dark) // Используем иконку для тёмной темы
        } else {
            playButton.setImageResource(R.drawable.icon_pause_button) // Используем иконку для светлой темы
        }
        mainThreadHandler?.post(
        playControl.createUpdateProgressTimeRunnable()
        )
    }

    override fun pausePlayer() {
        if (isDarkTheme()) {
            playButton.setImageResource(R.drawable.icon_play_button_dark) // Используем иконку для тёмной темы
        } else {
            playButton.setImageResource(R.drawable.icon_play_button) // Используем иконку для светлой темы
        }
    }

    private fun isDarkTheme(): Boolean {
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
    }

    override fun progressTimeViewUpdate(progressTime: String) {
        progressTimeView.text = progressTime
    }

    override fun playButtonEnabled() {
        playButton.isEnabled = true
    }

    override fun postDelayed(runnable: Runnable) {
        mainThreadHandler?.postDelayed(runnable, DELAY_MILLIS)
    }

    override fun removeCallbacks(runnable: Runnable) {
        mainThreadHandler?.removeCallbacks(runnable)
    }

    override fun onPause() {
        super.onPause()
        playControl.pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        playControl.release()
    }
}
