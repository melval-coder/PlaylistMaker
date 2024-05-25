package com.practicum.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

const val TRACKS_KEY = "track_key"
const val MAXIMUM = 10

class SearchHistory {

    // Метод для добавления трека в историю поиска
    fun setTrack(track: Track, sharedPreferences: SharedPreferences) {
        val tracks = read(sharedPreferences)
        if (!tracks.remove(track) && tracks.size >= MAXIMUM) tracks.removeAt(MAXIMUM - 1)
        tracks.add(0, track)
        write(sharedPreferences, tracks)
    }

    // Метод для чтения истории треков из SharedPreferences
    fun read(sharedPreferences: SharedPreferences): MutableList<Track> {
        val json = sharedPreferences.getString(TRACKS_KEY, null) ?: return mutableListOf()
        val listOfMyClassObject: Type = object : TypeToken<ArrayList<Track>>() {}.type
        return Gson().fromJson(json, listOfMyClassObject)
    }

    // Метод для записи обновленной истории треков в SharedPreferences
    private fun write(sharedPreferences: SharedPreferences, tracks: MutableList<Track>) {
        val json = Gson().toJson(tracks)
        sharedPreferences.edit()
            .putString(TRACKS_KEY, json)
            .apply()
    }

    // Метод для очистки истории треков
    fun clear(sharedPreferences: SharedPreferences){
        sharedPreferences.edit()
            .remove(TRACKS_KEY)
            .apply()
    }
}