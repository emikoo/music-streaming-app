package com.example.music_streaming_android.data.repository

import android.util.Log
import com.example.music_streaming_android.data.api.MusicApiService
import com.example.music_streaming_android.data.api.RetrofitClient
import com.example.music_streaming_android.data.models.Playlist
import com.example.music_streaming_android.data.models.PlaylistDetails
import com.example.music_streaming_android.data.models.Song
import com.example.music_streaming_android.data.models.SongDetails
import com.example.music_streaming_android.data.models.TopPlaylist
import com.example.music_streaming_android.data.models.TopSong
import com.example.music_streaming_android.data.models.UserDetails
import com.example.music_streaming_android.data.models.UserPlaytime

class MusicRepository(private val apiService: MusicApiService = RetrofitClient.musicApiService) {
    private val cache = mutableMapOf<String, Pair<Any, Long>>()
    private val cacheTtl = 5 * 60 * 1000L

    private fun <T> getCachedData(key: String): T? {
        val cached = cache[key]
        return if (cached != null && System.currentTimeMillis() - cached.second < cacheTtl) {
            cached.first as? T
        } else {
            cache.remove(key)
            null
        }
    }

    private fun <T> setCachedData(key: String, data: T) {
        cache[key] = Pair(data as Any, System.currentTimeMillis())
    }

    suspend fun getTopSongs(): List<TopSong> {
        val cacheKey = "top_songs"
        getCachedData<List<TopSong>>(cacheKey)?.let { return it }

        return try {
            val response = apiService.getTopSongs()
            if (response.isSuccessful) {
                val data = response.body() ?: emptyList()
                setCachedData(cacheKey, data)
                data
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error fetching top songs", e)
            emptyList()
        }
    }

    suspend fun getTopPlaylists(): List<TopPlaylist> {
        return try {
            val response = apiService.getTopPlaylists()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("MusicRepository", "Error fetching top playlists: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error fetching top playlists", e)
            emptyList()
        }
    }

    suspend fun getTopUsers(): List<UserPlaytime> {
        return try {
            val response = apiService.getTopUsers()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error fetching top users", e)
            emptyList()
        }
    }

    suspend fun getUserPlaytime(): List<UserPlaytime> {
        return try {
            val response = apiService.getUserPlaytime()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPlaylists(): List<Playlist> {
        val response = apiService.getPlaylists()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        }
        return emptyList()
    }

    suspend fun createPlaylist(name: String, description: String = "", cover: String? = null): Playlist? {
        val playlistMap = mutableMapOf<String, Any>("name" to name)
        if (description.isNotBlank()) {
            playlistMap["description"] = description
        }
        if (cover != null) {
            playlistMap["cover"] = cover
        }

        val response = apiService.createPlaylist(playlistMap)
        if (response.isSuccessful) {
            return response.body()
        }
        return null
    }

    suspend fun getAllSongs(): List<Song> {
        return try {
            val response = apiService.getSongs()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error fetching all songs", e)
            emptyList()
        }
    }

    suspend fun searchSongs(query: String): List<Song> {
        return try {
            val response = apiService.getSongs(search = query)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error searching songs", e)
            emptyList()
        }
    }

    suspend fun getSongDetails(songId: Int): SongDetails? {
        return try {
            val response = apiService.getSongDetails(songId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPlaylistDetails(playlistId: Int): PlaylistDetails? {
        return try {
            val response = apiService.getPlaylistDetails(playlistId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserDetails(userId: Int): UserDetails? {
        return try {
            val response = apiService.getUserDetails(userId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}