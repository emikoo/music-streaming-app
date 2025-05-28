package com.example.music_streaming_android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.music_streaming_android.PlaylistItem
import com.example.music_streaming_android.SongItem
import com.example.music_streaming_android.TopPlaylistItem
import com.example.music_streaming_android.TopSongItem
import com.example.music_streaming_android.TopUserItem
import com.example.music_streaming_android.ui.viewmodel.MusicViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: MusicViewModel = viewModel()) {
    val topSongs = viewModel.topSongs.value
    val topPlaylists = viewModel.topPlaylists.value
    val userPlaytime = viewModel.userPlaytime.value
    viewModel.topUsers.value
    val isLoading = viewModel.isLoading.value
    val error = viewModel.error.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        item {
            Text(
                "Good evening",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        } else if (error != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: $error", color = Color.Red)
                }
            }
        } else {
            item {
                Text(
                    "Top Songs",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(topSongs) { topSong ->
                        TopSongItem(topSong, navController)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (userPlaytime.isNotEmpty()) {
                item {
                    Text(
                        "User Playtime",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(userPlaytime) { userTime ->
                            TopUserItem(userTime, navController)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            item {
                Text(
                    "Top Playlists",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(topPlaylists) { topPlaylist ->
                        TopPlaylistItem(topPlaylist, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchScreen(navController: NavController, viewModel: MusicViewModel = viewModel()) {
    var searchText by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults
    val allSongs by viewModel.allSongs

    LaunchedEffect(Unit) {
        viewModel.loadAllSongs()
    }

    LaunchedEffect(allSongs) {
        if (searchText.isBlank()) {
            viewModel.searchResults.value = allSongs
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchText, onValueChange = {
            searchText = it
            viewModel.searchSongs(it)
        }, label = { Text("Search songs...", color = Color.Gray) }, leadingIcon = {
            Icon(
                Icons.Default.Search, contentDescription = "Search", tint = Color.Gray
            )
        }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF1DB954),
            unfocusedBorderColor = Color.Gray
        )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(searchResults) { song ->
                SongItem(song = song, navController = navController)
            }
        }
    }
}


@Composable
fun LibraryScreen(navController: NavController, viewModel: MusicViewModel = viewModel()) {
    val playlists = viewModel.allPlaylists.value
    viewModel.users.value
    val isCreatingPlaylist = viewModel.isCreatingPlaylist.value
    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadPlaylists()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Your Playlists",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { showCreateDialog = true },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1DB954))
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create Playlist",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        "Create Playlist",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        items(playlists) { playlist ->
            PlaylistItem(playlist = playlist, navController = navController)
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Playlist", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist Name", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF1DB954),
                        unfocusedBorderColor = Color.Gray
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            viewModel.createPlaylist(playlistName, "")
                            playlistName = ""
                            showCreateDialog = false
                        }
                    }, enabled = !isCreatingPlaylist
                ) {
                    if (isCreatingPlaylist) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp), color = Color(0xFF1DB954)
                        )
                    } else {
                        Text("Create", color = Color(0xFF1DB954))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF282828)
        )
    }
}