package com.darkaquadigital.audihive

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FeaturedPlayList
import androidx.compose.material.icons.automirrored.outlined.FeaturedPlayList
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.darkaquadigital.audihive.data.Album
import com.darkaquadigital.audihive.data.Artist
import com.darkaquadigital.audihive.data.MusicLoader
import com.darkaquadigital.audihive.data.PlaylistViewModel
import com.darkaquadigital.audihive.data.PlaylistViewModelFactory
import com.darkaquadigital.audihive.data.QueueViewModel
import com.darkaquadigital.audihive.data.QueueViewModelFactory
import com.darkaquadigital.audihive.data.Song
import com.darkaquadigital.audihive.data.SongViewModel
import com.darkaquadigital.audihive.data.SongViewModelFactory
import com.darkaquadigital.audihive.database.MusicDatabase
import com.darkaquadigital.audihive.database.PlaylistRepository
import com.darkaquadigital.audihive.database.PlaylistWithSongs
import com.darkaquadigital.audihive.database.SongRepository
import com.darkaquadigital.audihive.player.MusicPlayer
import com.darkaquadigital.audihive.player.PlayerViewModel
import com.darkaquadigital.audihive.player.PlayerViewModelFactory
import com.darkaquadigital.audihive.ui.screens.AlbumDetailScreen
import com.darkaquadigital.audihive.ui.screens.AlbumListScreen
import com.darkaquadigital.audihive.ui.screens.ArtistDetailScreen
import com.darkaquadigital.audihive.ui.screens.ArtistListScreen
import com.darkaquadigital.audihive.ui.screens.PlaylistDetailScreen
import com.darkaquadigital.audihive.ui.screens.PlaylistScreen
import com.darkaquadigital.audihive.ui.screens.QueueScreen
import com.darkaquadigital.audihive.ui.screens.SongsScreen
import com.darkaquadigital.audihive.ui.theme.AudiHiveTheme
import com.darkaquadigital.audihive.utilities.getSongEntity
import com.darkaquadigital.audihive.utilities.toSong
import com.darkaquadigital.audihive.utilities.toSongs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val isDebugging = false

    private val songs = mutableStateListOf<Song>()
    private val albums = mutableListOf<Album>()
    private val artists = mutableListOf<Artist>()
    private lateinit var songRepository: SongRepository
    private lateinit var playlistRepository: PlaylistRepository

    private lateinit var songViewModel: SongViewModel
    private lateinit var playlistViewModel: PlaylistViewModel
    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var queueViewModel: QueueViewModel

    private var isLoading = mutableStateOf(true)

    private var songsLoaded: Boolean = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val dbPath = this.getDatabasePath("music_database").absolutePath
        Log.d("DatabasePath", "DB location: $dbPath")

        val database = MusicDatabase.getDatabase(this)
        songRepository = SongRepository(database.songDao())
        playlistRepository = PlaylistRepository(database.songDao(), database.playlistDao())
        songViewModel = ViewModelProvider(this, SongViewModelFactory(songRepository))[SongViewModel::class.java]
        playlistViewModel = ViewModelProvider(this, PlaylistViewModelFactory(playlistRepository, database.playlistDao()))[PlaylistViewModel::class.java]
        playerViewModel = ViewModelProvider(this, PlayerViewModelFactory(application))[PlayerViewModel::class.java]
        queueViewModel = ViewModelProvider(this, QueueViewModelFactory(playerViewModel))[QueueViewModel::class.java]

        setContent {
            val playlists by playlistViewModel.allPlaylists.collectAsState()
            Log.d("MainActivity", "Loaded playlists: ${playlists.size}")
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    MainScreen(navController)
                }
                composable("album/{albumId}") { backStackEntry ->
                    val albumId = backStackEntry.arguments?.getString("albumId")?.toIntOrNull()
                    val album = albums.find { it.id == albumId }
                    val albumSongs = songs.filter { it.album == album?.name }
                    if (album != null) {
                        AlbumDetailScreen(
                            album,
                            albumSongs,
                            playerViewModel,
                            playlistViewModel,
                            onSongClick = { song -> playerViewModel.playSong(song) },
                            onPlayAll = { playerViewModel.playAll(albumSongs) },
                            onShuffle = { playerViewModel.shufflePlay(albumSongs) }
                       )
                    }
                }
                composable("artist/{artistId}") { backStackEntry ->
                    val artistId = backStackEntry.arguments?.getString("artistId")?.toIntOrNull()
                    val artist = artists.find { it.id == artistId }
                    val artistAlbums = albums.filter { it.artist == artist?.name }
                    val artistSongs = songs.filter { it.artist == artist?.name }
                    if (artist != null) {
                        ArtistDetailScreen(
                            artist = artist,
                            albums = artistAlbums,
                            songs = artistSongs,
                            playerViewModel = playerViewModel,
                            playlistViewModel = playlistViewModel,
                            onSongClick = { song -> playerViewModel.playSong(song) },
                            onAlbumClick = { album -> navController.navigate("album/${album.id}") },
                            onPlayAll = { playerViewModel.playAll(artistSongs) },
                            onShuffle = { playerViewModel.shufflePlay(artistSongs) }
                        )
                    }
                }
                composable(
                    route = "playlist/{playlistId}",
                    arguments = listOf(navArgument("playlistId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getInt("playlistId") ?: -1
                    var playlistWithSongs by remember { mutableStateOf<PlaylistWithSongs?>(null) }

                    LaunchedEffect(playlistId) {
                        playlistViewModel.getPlaylistWithSongs(playlistId) { result ->
                            playlistWithSongs = result
                        }
                    }

                    playlistWithSongs?.let { playlist ->
                        val songList: List<Song> = playlist.songs.toSongs()
                        PlaylistDetailScreen(
                            playlistId,
                            playlistViewModel,
                            onSongClick = { song -> playerViewModel.playSong(song) },
                            onPlaylistOptions = { /* Handle adding to playlist */ },
                            onPlayAll = { playerViewModel.playAll(songList) },
                            onShuffle = { playerViewModel.shufflePlay(songList) }
                        )
                    }
                }

            }
        }

        requestPermissionAndLoadSongs()
    }

    /* Tabs */

    enum class HomeTabs (
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector,
        val text: String
    ) {
        Songs (
            unselectedIcon = Icons.Outlined.MusicNote,
            selectedIcon = Icons.Filled.MusicNote,
            text = "Songs"
        ),
        Albums (
            unselectedIcon = Icons.Outlined.LibraryMusic,
            selectedIcon = Icons.Filled.LibraryMusic,
            text = "Albums"
        ),
        Artists (
            unselectedIcon = Icons.Outlined.LibraryMusic,
            selectedIcon = Icons.Filled.LibraryMusic,
            text = "Artists"
        ),
        Playlists (
            unselectedIcon = Icons.AutoMirrored.Outlined.FeaturedPlayList,
            selectedIcon = Icons.AutoMirrored.Filled.FeaturedPlayList,
            text = "Playlists"
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    fun MainScreen(navController: NavController) {
        val scope = rememberCoroutineScope()
        val pagerState = rememberPagerState(pageCount = { HomeTabs.entries.size})
        val selectedTabIndex = remember { derivedStateOf { pagerState.currentPage }}

        val currentSong by playerViewModel.currentSong.collectAsState()
        val isPlaying by playerViewModel.isPlaying.collectAsState()

        var showQueue by remember { mutableStateOf(false) }

        if (showQueue) {
            ModalBottomSheet(
                onDismissRequest = { showQueue = false }
            ) {
                QueueScreen(
                    queue = playerViewModel.currentQueue.value
                )
            }
        }


        AudiHiveTheme {
            Scaffold { paddingValues ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues) // Apply scaffold's padding properly
                ) {
                    // Mini Player Placeholder - Always occupies space
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .defaultMinSize(minHeight = 70.dp)
                            //.height(64.dp) // Ensures it always reserves space
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF202020), Color(0xFF404040))
                                )
                            ),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var isExpanded by remember { mutableStateOf(false) }
                        fun toggleExpansion() {
                            isExpanded = !isExpanded
                            Log.d("MainActivity", "Toggle Expansion: $isExpanded")
                        }
                        if (currentSong != null) {
                            MusicPlayer(
                                song = currentSong,
                                isPlaying = isPlaying,
                                onPlayPauseClick = { playerViewModel.togglePlayPause() },
                                getSongProgress = { playerViewModel.getSongProgress() },
                                isExpanded = isExpanded,
                                onExpandToggle = { toggleExpansion() },
                                onNextClick = { playerViewModel.nextSong() },
                                onPreviousClick = { playerViewModel.previousSong() },
                                onShuffleClick = { playerViewModel.shufflePlay(playerViewModel.songs.value) },
                                onRepeatClick = { playerViewModel.toggleRepeat() },
                                onShowQueue = { showQueue = true },
                                playerViewModel = playerViewModel
                            )
                        } else {
                            Text(
                                text = "Not Playing",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Tabs and Content
                    TabRow(selectedTabIndex = selectedTabIndex.value) {
                        HomeTabs.entries.forEachIndexed { index, currentTab ->
                            Tab(
                                selected = selectedTabIndex.value == index,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                text = { Text(text = currentTab.text) },
                                selectedContentColor = MaterialTheme.colorScheme.primary,
                                unselectedContentColor = MaterialTheme.colorScheme.secondary,
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTabIndex.value == index)
                                            currentTab.selectedIcon else currentTab.unselectedIcon,
                                        contentDescription = "Tab Icon"

                                    )
                                }
                            )
                        }
                    }

                    val context = LocalContext.current.applicationContext as Application
                    val hasPermission = remember { mutableStateOf(false) }
                    val lifecycleOwner = LocalLifecycleOwner.current

                    // Permission launcher
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { granted ->
                        hasPermission.value = granted
                    }

                    // Observe lifecycle to refresh when returning from settings
                    LaunchedEffect(lifecycleOwner) {
                        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                            val granted = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.READ_MEDIA_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            hasPermission.value = granted

                            if (granted) {
                                if (!songsLoaded) {
                                    loadSongsFromDatabase()
                                }
                            }
                        }
                    }

                    // Request permission initially
                    LaunchedEffect(Unit) {
                        if (!hasPermission.value) {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                        }
                        else {
                            if (!songsLoaded) {
                                loadSongsFromDatabase()
                            }
                        }
                    }

                    // Content Section
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 64.dp) // Pushes content down
                    ) { page ->
                        when (HomeTabs.entries[page]) {
                            HomeTabs.Songs -> SongsScreen(songs, playerViewModel, playlistViewModel, {}, isLoading, { playerViewModel.playAll(songs) }, {playerViewModel.shufflePlay(songs) })
                            HomeTabs.Albums -> AlbumListScreen(albums = albums, navController = navController)
                            HomeTabs.Artists -> ArtistListScreen(artists = artists, navController = navController)
                            HomeTabs.Playlists -> PlaylistScreen(playlists = playlistViewModel.allPlaylists, navController = navController, playlistViewModel = playlistViewModel)
                        }
                    }
                }
            }
        }

    }

    /* Permissions */

    private fun requestPermissionAndLoadSongs() {
        val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val imagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        // Check if audio permission is granted
        if (ContextCompat.checkSelfPermission(this, audioPermission) != PackageManager.PERMISSION_GRANTED) {
            // If audio permission is not granted, request it
            audioPermissionLauncher.launch(audioPermission)
        } else {
            // If audio permission is granted, request image permission
            if (isDebugging) Log.d("MainActivity", "Audio permission already granted, requesting image permission...")
            requestImagePermission()
        }
    }

    private val audioPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Audio permission granted, now request for images
            requestImagePermission()
        } else {
            // Permission denied, handle accordingly
            if (isDebugging) Log.e("MainActivity", "Audio Permission Denied")
        }
    }

    private val imagePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Image permission granted, now load songs
            if (!songsLoaded) {
                loadSongsFromDatabase()
            }
        } else {
            // Permission denied, handle accordingly
            if (isDebugging) Log.e("MainActivity", "Image Permission Denied")
        }
    }

    private fun requestImagePermission() {
        val imagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        // Check if image permission is granted
        if (ContextCompat.checkSelfPermission(this, imagePermission) != PackageManager.PERMISSION_GRANTED) {
            // If image permission is not granted, request it
            imagePermissionLauncher.launch(imagePermission)
        } else {
            // If both permissions are granted, load songs
            if (isDebugging) Log.d("MainActivity", "Image permission already granted, loading songs...")
            if (!songsLoaded) {
                loadSongsFromDatabase()
            }
        }
    }

    private fun loadSongsFromDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val allStoredSongs = songRepository.getAllSongs().first() // Collects the current list once

            songs.clear()
            songs.addAll(allStoredSongs.map { it.toSong() }) // Convert to your app's `Song` model


            val albumMap = mutableMapOf<String, MutableList<Song>>()
            val artistMap = mutableMapOf<String, MutableList<Song>>()

            for (song in allStoredSongs) {
                albumMap.getOrPut(song.album) { mutableListOf() }.add(song.toSong())
                artistMap.getOrPut(song.artist) { mutableListOf() }.add(song.toSong())
            }

            albums.clear()
            artists.clear()

            for ((albumName, songList) in albumMap) {
                albums.add(
                    Album(
                        id = albumName.hashCode(),
                        name = albumName,
                        artist = songList.first().artist,
                        coverImage = songList.firstNotNullOfOrNull { it.albumImagePath } ?: ""
                    )
                )
            }

            for ((artistName, songList) in artistMap) {
                artists.add(
                    Artist(
                        id = artistName.hashCode(),
                        name = artistName,
                        image = songList.firstNotNullOfOrNull { it.albumImagePath } ?: ""
                    )
                )
            }

            withContext(Dispatchers.Main) {
                isLoading.value = false
                songsLoaded = true
            }

            // Then continue to scan and sync storage
            scanAndUpdateFromStorage()
        }
    }


    private fun scanAndUpdateFromStorage() {
        lifecycleScope.launch(Dispatchers.IO) {
            val storedSongs = songRepository.getAllStoredPaths().toMutableSet()
            val musicFiles = MusicLoader().getAllSongs(this@MainActivity)

            var updated = false

            for (file in musicFiles) {
                val existingSong = songRepository.getSongByPath(file.data)
                val lastModified = file.lastModified
                val fileSize = file.fileSize

                if (existingSong != null) {
                    if (existingSong.lastModified != lastModified || existingSong.fileSize != fileSize) {
                        val updatedSong = existingSong.copy(
                            lastModified = lastModified,
                            fileSize = fileSize
                        )
                        songRepository.updateSong(updatedSong)
                        updated = true
                    }
                    storedSongs.remove(file.data)
                } else {
                    val newSong = getSongEntity(file)
                    songRepository.insertSong(newSong)
                    updated = true
                }
            }

            for (oldPath in storedSongs) {
                val oldSong = songRepository.getSongByPath(oldPath)
                if (oldSong != null) songRepository.deleteSong(oldSong)
                updated = true
            }

            if (updated) {
                // Re-load from database on main thread
                withContext(Dispatchers.Main) {
                    loadSongsFromDatabase()
                }
            }
        }
    }
}
