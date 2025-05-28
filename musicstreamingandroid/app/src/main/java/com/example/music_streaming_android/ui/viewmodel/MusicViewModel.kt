package com.example.music_streaming_android.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music_streaming_android.data.models.Artist
import com.example.music_streaming_android.data.models.Playlist
import com.example.music_streaming_android.data.models.PlaylistDetails
import com.example.music_streaming_android.data.models.Song
import com.example.music_streaming_android.data.models.SongDetails
import com.example.music_streaming_android.data.models.TopPlaylist
import com.example.music_streaming_android.data.models.TopSong
import com.example.music_streaming_android.data.models.User
import com.example.music_streaming_android.data.models.UserDetails
import com.example.music_streaming_android.data.models.UserPlaytime
import com.example.music_streaming_android.data.repository.MusicRepository
import kotlinx.coroutines.launch

class MusicViewModel() : ViewModel() {
    private val repository = MusicRepository()

    val topSongs = mutableStateOf<List<TopSong>>(emptyList())
    val allPlaylists = mutableStateOf<List<Playlist>>(emptyList())
    val topPlaylists = mutableStateOf<List<TopPlaylist>>(emptyList())
    val userPlaytime = mutableStateOf<List<UserPlaytime>>(emptyList())
    val topUsers = mutableStateOf<List<UserPlaytime>>(emptyList())
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    val users = mutableStateOf<List<User>>(emptyList())
    val isCreatingPlaylist = mutableStateOf(false)

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            try {
                topSongs.value = repository.getTopSongs()
                allPlaylists.value = repository.getPlaylists()
                topPlaylists.value =
                    repository.getTopPlaylists().sortedByDescending { it.playCount }.take(10)
                        .map { playlist ->
                            TopPlaylist(
                                id = playlist.id,
                                name = playlist.name,
                                playCount = playlist.playCount,
                                cover = playlist.cover
                            )
                        }
                userPlaytime.value = repository.getUserPlaytime()
                topUsers.value = repository.getTopUsers()
            } catch (e: Exception) {
                error.value = "Failed to load data: ${e.message ?: "Unknown error"}"
                useSampleData()
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun useSampleData() {
        val sampleArtists = listOf(
            Artist(1, "Ed Sheeran", "UK", null),
            Artist(2, "The Weeknd", "Canada", null),
            Artist(3, "Tones and I", "Australia", null),
            Artist(4, "Lewis Capaldi", "UK", null),
            Artist(5, "Billie Eilish", "USA", null)
        )

        val sampleSongs = listOf(
            Song(
                1,
                "Shape of You",
                1,
                sampleArtists[0],
                null,
                235,
                "https://picsum.photos/300/300?random=1"
            ),
            Song(
                2,
                "Blinding Lights",
                2,
                sampleArtists[1],
                null,
                200,
                "https://picsum.photos/300/300?random=2"
            ),
            Song(
                3,
                "Dance Monkey",
                3,
                sampleArtists[2],
                null,
                210,
                "https://picsum.photos/300/300?random=3"
            ),
            Song(
                4,
                "Someone You Loved",
                4,
                sampleArtists[3],
                null,
                182,
                "https://picsum.photos/300/300?random=4"
            ),
            Song(
                5,
                "Bad Guy",
                5,
                sampleArtists[4],
                null,
                194,
                "https://picsum.photos/300/300?random=5"
            )
        )

        val sampleTopSongs = sampleSongs.mapIndexed { index, song ->
            TopSong(
                id = song.id,
                title = song.title,
                artist = song.artist?.name ?: "Unknown Artist",
                playCount = 100 - (index * 10),
                albumCover = song.albumCover
            )
        }

        val samplePlaylists = listOf(
            Playlist(
                1,
                "Top Hits 2023",
                true,
                null,
                "2023-01-01",
                "System",
                "https://picsum.photos/300/300?random=31",
                150
            ),
            Playlist(
                2,
                "Chill Vibes",
                false,
                1,
                "2023-02-15",
                "john_doe",
                "https://picsum.photos/300/300?random=32",
                120
            ),
            Playlist(
                3,
                "Workout Mix",
                false,
                2,
                "2023-03-20",
                "jane_smith",
                "https://picsum.photos/300/300?random=33",
                95
            ),
            Playlist(
                4,
                "Road Trip",
                false,
                1,
                "2023-04-10",
                "john_doe",
                "https://picsum.photos/300/300?random=34",
                80
            ),
            Playlist(
                5,
                "Study Focus",
                true,
                null,
                "2023-05-05",
                "System",
                "https://picsum.photos/300/300?random=35",
                65
            )
        )

        allPlaylists.value = samplePlaylists
        topPlaylists.value =
            samplePlaylists.sortedByDescending { it.playCount }.take(10).map { playlist ->
                    TopPlaylist(
                        id = playlist.id,
                        name = playlist.name,
                        playCount = playlist.playCount,
                        cover = playlist.cover
                    )
                }

        val sampleUsers = listOf(
            User(
                1,
                "john_doe",
                "john@example.com",
                "https://picsum.photos/200/200?random=51",
                "2023-01-01"
            ),
            User(
                2,
                "jane_smith",
                "jane@example.com",
                "https://picsum.photos/200/200?random=52",
                "2023-02-01"
            ),
            User(
                3,
                "mike_johnson",
                "mike@example.com",
                "https://picsum.photos/200/200?random=53",
                "2023-03-01"
            )
        )

        val sampleUserPlaytime = sampleUsers.mapIndexed { index, user ->
            UserPlaytime(
                user = user, totalPlaytime = 3600 - (index * 600)
            )
        }

        topSongs.value = sampleTopSongs
        userPlaytime.value = sampleUserPlaytime
        topUsers.value = sampleUserPlaytime.take(3)
    }

    val allSongs = mutableStateOf<List<Song>>(emptyList())
    val searchResults = mutableStateOf<List<Song>>(emptyList())
    val searchQuery = mutableStateOf("")

    fun loadAllSongs() {
        viewModelScope.launch {
            allSongs.value = repository.getAllSongs()
        }
    }

    fun searchSongs(query: String) {
        searchQuery.value = query
        if (query.isBlank()) {
            searchResults.value = allSongs.value
        } else {
            viewModelScope.launch {
                searchResults.value = repository.searchSongs(query)
            }
        }
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            allPlaylists.value = repository.getPlaylists()
            topPlaylists.value =
                repository.getTopPlaylists().sortedByDescending { it.playCount }.take(10)
                    .map { playlist ->
                        TopPlaylist(
                            id = playlist.id,
                            name = playlist.name,
                            playCount = playlist.playCount,
                            cover = playlist.cover
                        )
                    }
        }
    }

    fun createPlaylist(name: String, description: String = "", cover: String? = null) {
        viewModelScope.launch {
            isCreatingPlaylist.value = true
            try {
                val newPlaylist = repository.createPlaylist(name, description, cover)
                if (newPlaylist != null) {
                    loadPlaylists()
                }
            } catch (e: Exception) {
                error.value = "Failed to create playlist: ${e.message}"
            } finally {
                isCreatingPlaylist.value = false
            }
        }
    }

    val songDetails = mutableStateOf<SongDetails?>(null)
    val playlistDetails = mutableStateOf<PlaylistDetails?>(null)
    val userDetails = mutableStateOf<UserDetails?>(null)

    fun loadSongDetails(songId: Int) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                songDetails.value = repository.getSongDetails(songId)
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loadPlaylistDetails(playlistId: Int) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                playlistDetails.value = repository.getPlaylistDetails(playlistId)
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loadUserDetails(userId: Int) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                userDetails.value = repository.getUserDetails(userId)
            } catch (e: Exception) {
                error.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }
}