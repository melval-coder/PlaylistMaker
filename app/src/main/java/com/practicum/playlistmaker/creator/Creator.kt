package com.practicum.playlistmaker.creator

import android.content.Context
import com.practicum.playlistmaker.data.Player
import com.practicum.playlistmaker.data.SearchHistory
import com.practicum.playlistmaker.data.TracksRepositoryImpl
import com.practicum.playlistmaker.data.network.RetrofitNetworkClient
import com.practicum.playlistmaker.domain.api.OneTrackRepository
import com.practicum.playlistmaker.domain.api.TrackHistoryRepository
import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.impl.TracksInteractorImpl
import com.practicum.playlistmaker.domain.useCase.PlayControlImpl
import com.practicum.playlistmaker.presentation.AudioPlayerActivity

object Creator {
    private fun getTrackRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient())
    }

    fun provideTrackInteractor(): TracksInteractor {
        return TracksInteractorImpl(getTrackRepository())
    }

    fun createPlayControl(playerPresenter: AudioPlayerActivity): PlayControlImpl {
        return PlayControlImpl(Player(), playerPresenter)
    }

    fun getOneTrackRepository(context: Context): OneTrackRepository {
        return SearchHistory(context)
    }

    fun getHistoryRepository(context: Context): TrackHistoryRepository {
        return SearchHistory(context)
    }
}