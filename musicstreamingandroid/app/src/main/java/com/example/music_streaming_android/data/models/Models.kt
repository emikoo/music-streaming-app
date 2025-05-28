package com.example.music_streaming_android.data.models

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val username: String?,
    val email: String,
    @SerializedName("profile_image") val profileImage: String?,
    @SerializedName("created_at") val createdAt: String
)

data class Artist(
    val id: Int,
    val name: String,
    val country: String?,
    @SerializedName("profile_image") val profileImage: String?
)

data class Song(
    val id: Int,
    val title: String,
    @SerializedName("artist_id") val artistId: Int,
    val artist: Artist?,
    @SerializedName("artist_name") val artistName: String?,
    val duration: Int,
    @SerializedName("album_cover") val albumCover: String?
) {
    val displayArtistName: String
        get() = artist?.name ?: artistName ?: "Unknown Artist"
}

data class Playlist(
    val id: Int,
    val name: String,
    @SerializedName("is_curated") val isCurated: Boolean,
    @SerializedName("created_by") val createdBy: Int?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("creator_username") val creatorUsername: String?,
    val cover: String?,
    @SerializedName("play_count") val playCount: Int = 0,
    val description: String? = null
)

data class TopPlaylist(
    val id: Int,
    val name: String?,
    @SerializedName("play_count") val playCount: Int,
    val cover: String?
)

data class TopSong(
    val id: Int,
    val title: String?,
    @SerializedName("artist_name") val artist: String?,
    @SerializedName("play_count") val playCount: Int,
    @SerializedName("album_cover") val albumCover: String?
)

data class UserPlaytime(
    val user: User,
    @SerializedName("total_playtime") val totalPlaytime: Int
) {
    val username: String get() = user.username ?: " "
}

data class SongDetails(
    val id: Int,
    val title: String,
    @SerializedName("artist_id") val artistId: Int,
    val duration: Int,
    @SerializedName("album_cover") val albumCover: String?,
    @SerializedName("artist_name") val artistName: String,
    @SerializedName("artist_country") val artistCountry: String?,
    @SerializedName("artist_image") val artistImage: String?,
    @SerializedName("play_count") val playCount: Int
)

data class PlaylistDetails(
    val id: Int,
    val name: String,
    @SerializedName("is_curated") val isCurated: Boolean,
    @SerializedName("created_by") val createdBy: Int?,
    @SerializedName("created_at") val createdAt: String,
    val cover: String?,
    @SerializedName("creator_username") val creatorUsername: String?,
    @SerializedName("creator_image") val creatorImage: String?,
    val songs: List<Song>,
    @SerializedName("song_count") val songCount: Int,
    @SerializedName("total_plays") val totalPlays: Int,
    val description: String? = null
)

data class UserDetails(
    val id: Int,
    val username: String?,
    val email: String,
    @SerializedName("profile_image") val profileImage: String?,
    @SerializedName("created_at") val createdAt: String,
    val playlists: List<Playlist>,
    @SerializedName("playlist_count") val playlistCount: Int,
    @SerializedName("total_playtime") val totalPlaytime: Int,
    @SerializedName("top_songs") val topSongs: List<TopSong>
)