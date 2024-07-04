package com.practicum.playlistmaker.data

import android.media.MediaPlayer
import com.practicum.playlistmaker.domain.PlayerInteractor
import com.practicum.playlistmaker.domain.models.Track

class PlayerInteractorImpl : PlayerInteractor {
    private var mediaPlayer = MediaPlayer()

    override fun preparePlayer(
        item: Track,
        prepareCallback: () -> Unit,
        completeCallback: () -> Unit
    ) {

        mediaPlayer.setDataSource(item.previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            prepareCallback()
        }
        mediaPlayer.setOnCompletionListener {
            completeCallback()
        }
    }

    override fun startPlayer() {
        mediaPlayer.start()
    }

    override fun pausePlayer() {
        mediaPlayer.pause()
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    override fun release() {
        mediaPlayer.release()
    }
}