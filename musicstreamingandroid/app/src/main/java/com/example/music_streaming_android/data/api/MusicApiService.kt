package com.example.music_streaming_android.data.api

import com.example.music_streaming_android.data.models.Playlist
import com.example.music_streaming_android.data.models.PlaylistDetails
import com.example.music_streaming_android.data.models.Song
import com.example.music_streaming_android.data.models.SongDetails
import com.example.music_streaming_android.data.models.TopPlaylist
import com.example.music_streaming_android.data.models.TopSong
import com.example.music_streaming_android.data.models.User
import com.example.music_streaming_android.data.models.UserDetails
import com.example.music_streaming_android.data.models.UserPlaytime
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MusicApiService {
    @GET("top-songs")
    suspend fun getTopSongs(): Response<List<TopSong>>

    @GET("songs")
    suspend fun getSongs(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("search") search: String? = null
    ): Response<List<Song>>

    @GET("top-playlists")
    suspend fun getTopPlaylists(): Response<List<TopPlaylist>>

    @GET("user-playtime")
    suspend fun getUserPlaytime(): Response<List<UserPlaytime>>

    @GET("top-users")
    suspend fun getTopUsers(): Response<List<UserPlaytime>>

    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @POST("users")
    suspend fun createUser(@Body user: Map<String, Any>): Response<User>

    @GET("playlists")
    suspend fun getPlaylists(): Response<List<Playlist>>

    @POST("playlists")
    suspend fun createPlaylist(@Body playlist: Map<String, Any>): Response<Playlist>

    @GET("songs/{id}")
    suspend fun getSongDetails(@Path("id") songId: Int): Response<SongDetails>

    @GET("playlists/{id}")
    suspend fun getPlaylistDetails(@Path("id") playlistId: Int): Response<PlaylistDetails>

    @GET("users/{id}")
    suspend fun getUserDetails(@Path("id") userId: Int): Response<UserDetails>
}