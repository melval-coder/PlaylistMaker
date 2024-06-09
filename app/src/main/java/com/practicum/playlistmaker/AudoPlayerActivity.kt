package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerActivity : AppCompatActivity() {

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
            duration.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(item.trackTimeMillis)

            Glide.with(applicationContext)
                .load(item.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg"))
                .placeholder(R.drawable.icon_placeholder)
                .centerCrop()
                .into(artwork)
        }

        val imageBack = findViewById<ImageView>(R.id.backButton)
        imageBack.setOnClickListener { finish() }
    }
}
