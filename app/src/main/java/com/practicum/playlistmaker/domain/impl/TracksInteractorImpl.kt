package com.practicum.playlistmaker.domain.impl

import android.net.http.NetworkException
import android.os.Build
import androidx.annotation.RequiresExtension
import com.practicum.playlistmaker.domain.api.TracksInteractor
import com.practicum.playlistmaker.domain.api.TracksRepository
import java.util.concurrent.Executors

class TracksInteractorImpl(private val repository: TracksRepository) : TracksInteractor {
    private val executor = Executors.newCachedThreadPool()

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun searchTracks(
        expression: String,
        consumer: TracksInteractor.TracksConsumer,
        errorHandler: TracksInteractor.ErrorHandler
    ) {
        executor.execute {
            val result = try {
                repository.searchTracks(expression)
            } catch (e: NetworkException) {
                errorHandler.handle()
                return@execute
            }
            consumer.consume(result)
        }
    }
}