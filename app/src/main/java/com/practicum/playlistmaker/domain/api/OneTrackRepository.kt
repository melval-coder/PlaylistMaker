package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Track

interface OneTrackRepository {
    fun getTrack(): Track
}