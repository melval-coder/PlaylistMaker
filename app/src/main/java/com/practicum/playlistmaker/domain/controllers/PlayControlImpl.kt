package com.practicum.playlistmaker.domain.controllers

import com.practicum.playlistmaker.domain.PlayerInteractor
import com.practicum.playlistmaker.domain.PlayerPresenter
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.util.TimeFormat

class PlayControlImpl(val mediaPlayer: PlayerInteractor, val playerPresenter: PlayerPresenter) :
    PlayControl {

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
    }

    private var playerState = STATE_DEFAULT

    override fun preparePlayer(item: Track) {
        mediaPlayer.preparePlayer(item, this::setPlayerStatePrepared, this::setPlayerState)
    }

    private fun setPlayerState() {
        playerState = STATE_PREPARED
    }

    private fun setPlayerStatePrepared() {
        playerState = STATE_PREPARED
        playerPresenter.playButtonEnabled()
    }

    override fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }
            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
        }
    }

    private fun startPlayer() {
        mediaPlayer.startPlayer()
        playerPresenter.startPlayer()
        playerState = STATE_PLAYING
    }

    override fun pausePlayer() {
        mediaPlayer.pausePlayer()
        playerPresenter.pausePlayer()
        playerState = STATE_PAUSED
    }

    override fun createUpdateProgressTimeRunnable(): Runnable {
        return object : Runnable {
            override fun run() {
                when (playerState) {
                    STATE_PLAYING -> {
                        playerPresenter.progressTimeViewUpdate(TimeFormat.format(mediaPlayer.getCurrentPosition()))
                        playerPresenter.postDelayed(this)
                    }
                    STATE_PAUSED -> {
                        playerPresenter.removeCallbacks(this)
                    }
                    STATE_PREPARED -> {
                        playerPresenter.pausePlayer()
                        playerPresenter.progressTimeViewUpdate(TimeFormat.ZERO_TIME)
                        playerPresenter.removeCallbacks(this)
                    }
                }
            }
        }
    }

    override fun release() {
        mediaPlayer.release()
    }
}