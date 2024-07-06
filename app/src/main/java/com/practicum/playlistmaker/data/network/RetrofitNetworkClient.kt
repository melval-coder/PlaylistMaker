package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.NetworkClient
import com.practicum.playlistmaker.data.dto.Response
import com.practicum.playlistmaker.data.dto.SearchRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class RetrofitNetworkClient : NetworkClient {

    private val itunesBaseUrl = "https://itunes.apple.com"
    private val retrofit = Retrofit.Builder()
        .baseUrl(itunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesService = retrofit.create(SearchAPI::class.java)

    override fun doRequest(dto: Any): Response {
        return try {
            if (dto is SearchRequest) {
                val resp = itunesService.search(dto.expression).execute()
                val body = resp.body() ?: Response()
                body.apply { resultCode = resp.code() }
            } else {
                Response().apply { resultCode = 400 }
            }
        } catch (e: IOException) {
            // Логирование ошибок ввода-вывода
            e.printStackTrace()
            Response().apply { resultCode = 500 } // Internal Server Error
        } catch (e: Exception) {
            // Логирование других исключений
            e.printStackTrace()
            Response().apply { resultCode = 500 }
        }
    }
}