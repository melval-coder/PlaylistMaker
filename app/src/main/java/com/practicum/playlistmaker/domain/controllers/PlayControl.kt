package com.practicum.playlistmaker.domain.controllers

import com.practicum.playlistmaker.domain.models.Track

interface PlayControl {
    fun playbackControl()
    fun preparePlayer(item: Track)
    fun pausePlayer()
    fun createUpdateProgressTimeRunnable(): Runnable
    fun release()
}