package com.example.music_streaming_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.music_streaming_android.data.models.Playlist
import com.example.music_streaming_android.data.models.Song
import com.example.music_streaming_android.data.models.TopPlaylist
import com.example.music_streaming_android.data.models.TopSong
import com.example.music_streaming_android.data.models.UserPlaytime
import com.example.music_streaming_android.navigation.Screen
import com.example.music_streaming_android.screens.HomeScreen
import com.example.music_streaming_android.screens.LibraryScreen
import com.example.music_streaming_android.screens.PlaylistDetailScreen
import com.example.music_streaming_android.screens.SearchScreen
import com.example.music_streaming_android.screens.SongDetailScreen
import com.example.music_streaming_android.screens.UserDetailScreen
import com.example.music_streaming_android.ui.theme.MusicstreamingandroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicstreamingandroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    SpotifyApp()
                }
            }
        }
    }
}

@Composable
fun SpotifyApp() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home, Screen.Search, Screen.Library
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Green,
                            selectedTextColor = Color.Green,
                            unselectedIconColor = Color.White,
                            unselectedTextColor = Color.White,
                            indicatorColor = Color.Transparent
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        })
                }
            }
        }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Search.route) { SearchScreen(navController) }
            composable(Screen.Library.route) { LibraryScreen(navController) }
            composable("song_detail/{songId}") { backStackEntry ->
                val songId = backStackEntry.arguments?.getString("songId")?.toIntOrNull() ?: 0
                SongDetailScreen(songId = songId, navController = navController)
            }
            composable("playlist_detail/{playlistId}") { backStackEntry ->
                val playlistId =
                    backStackEntry.arguments?.getString("playlistId")?.toIntOrNull() ?: 0
                PlaylistDetailScreen(playlistId = playlistId, navController = navController)
            }
            composable("user_detail/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
                UserDetailScreen(userId = userId, navController = navController)
            }
        }
    }
}

@Composable
fun SongItem(song: Song, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                navController.navigate("song_detail/${song.id}")
            }, colors = CardDefaults.cardColors(containerColor = Color(0xFF282828))
    ) {
        Row(
            modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            if (!song.albumCover.isNullOrEmpty()) {
                AsyncImage(
                    model = song.albumCover,
                    contentDescription = "Album cover",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Face,
                    contentDescription = "Music",
                    modifier = Modifier.size(50.dp),
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = song.title, color = Color.White, fontWeight = FontWeight.Bold
                )
                Text(
                    text = song.displayArtistName, color = Color.Gray, fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun TopSongItem(topSong: TopSong, navController: NavController) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable {
                navController.navigate("song_detail/${topSong.id}")
            }, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        ) {
            if (!topSong.albumCover.isNullOrEmpty()) {
                AsyncImage(
                    model = topSong.albumCover,
                    contentDescription = "Album cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            topSong.title ?: "Unknown Title",
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "${topSong.artist ?: "Unknown Artist"} • ${topSong.playCount} plays",
            color = Color.Gray,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TopPlaylistItem(topPlaylist: TopPlaylist, navController: NavController) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable {
                navController.navigate("playlist_detail/${topPlaylist.id}")
            }, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        ) {
            if (!topPlaylist.cover.isNullOrEmpty()) {
                AsyncImage(
                    model = topPlaylist.cover,
                    contentDescription = "Playlist cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            topPlaylist.name ?: "Unknown Playlist",
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "${topPlaylist.playCount} plays",
            color = Color.Gray,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PlaylistItem(playlist: Playlist, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                navController.navigate("playlist_detail/${playlist.id}")
            }, colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!playlist.cover.isNullOrEmpty()) {
                AsyncImage(
                    model = playlist.cover,
                    contentDescription = "Playlist Cover",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF333333))
                ) {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = "Playlist",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    playlist.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (playlist.isCurated) "Curated Playlist" else "By ${playlist.creatorUsername ?: "Unknown"}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                if (playlist.playCount > 0) {
                    Text(
                        "${playlist.playCount} plays", color = Color.Gray, fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TopUserItem(userPlaytime: UserPlaytime, navController: NavController) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(160.dp)
            .clickable {
                navController.navigate("user_detail/${userPlaytime.user.id}")
            }, colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!userPlaytime.user.profileImage.isNullOrEmpty()) {
                AsyncImage(
                    model = userPlaytime.user.profileImage,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(30.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Face,
                    contentDescription = "Default profile",
                    modifier = Modifier.size(60.dp),
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userPlaytime.user.username ?: "Unknown User",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            val hours = userPlaytime.totalPlaytime / 3600
            val minutes = (userPlaytime.totalPlaytime % 3600) / 60
            Text(
                text = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m",
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}
