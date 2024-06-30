package com.practicum.playlistmaker.domain

import com.practicum.playlistmaker.domain.models.Track

interface PlayerInteractor {
    fun preparePlayer(track: Track, prepareCallback: () -> Unit, completeCallback: () -> Unit)
    fun startPlayer()
    fun pausePlayer()
    fun getCurrentPosition(): Int
    fun release()
}