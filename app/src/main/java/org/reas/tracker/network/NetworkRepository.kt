package org.reas.tracker.network

import android.util.Log
import org.reas.tracker.util.Secrets
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NetworkRepository {
    suspend fun getAlbumImageUrl(artist: String, album: String, size: String): String?
}

class RetrofitNetworkRepository : NetworkRepository {
    internal interface LastFMService {
        @Headers("Cache-Control: max-age=640000")
        @GET("/2.0")
        suspend fun getAlbumImages(
            @Query("method") method: String,
            @Query("api_key") apiKey: String,
            @Query("format") format: String,
            @Query("artist") artist: String,
            @Query("album") album: String,
        ): Response<LastFMAlbumInfoWrapper>
    }

    private val lastFMRetrofit = Retrofit.Builder()
        .baseUrl("https://ws.audioscrobbler.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val lastFMService = lastFMRetrofit.create(LastFMService::class.java)

    override suspend fun getAlbumImageUrl(artist: String, album: String, size: String): String? {
        Log.d(TAG, "getAlbumImageUrl $artist $album $size")
        val response = lastFMService.getAlbumImages(
            "album.getinfo", Secrets.LASTFM_API_KEY, "json",
            artist, album
        )
        if (!response.isSuccessful) {
            val stream = response.errorBody()!!.charStream()
            val errorText = stream.readText()
            stream.close()
            Log.w(TAG, "couldn't get album images for $artist - $album: " +
                    "${response.code()} $errorText")
            return null
        }
        Log.d(TAG, "response body ${response.body()}")
        return response.body()!!.album.image
            .firstOrNull { it.size == size }?.url
    }

    companion object {
        private const val TAG = "RetrofitNetworkRepository"
    }
}