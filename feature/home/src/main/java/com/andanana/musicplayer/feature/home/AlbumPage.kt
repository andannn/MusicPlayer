package com.andanana.musicplayer.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andanana.musicplayer.core.designsystem.component.AlbumCard

private const val TAG = "AlbumPage"

@Composable
fun AlbumPage(
    modifier: Modifier = Modifier,
    albumPageViewModel: AlbumPageViewModel = hiltViewModel()
) {
    val state by albumPageViewModel.albumPageUiState.collectAsState()

    AlbumPageContent(
        modifier = modifier,
        state = state
    )
}

@Composable
private fun AlbumPageContent(
    modifier: Modifier = Modifier,
    state: AlbumPageUiState
) {
    when (state) {
        AlbumPageUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        is AlbumPageUiState.Ready -> {
            val musicInfoList = state.infoList

            LazyVerticalGrid(
                modifier = modifier,
                columns = GridCells.Fixed(2)
            ) {
                items(
                    items = musicInfoList,
                    key = { it.albumId }
                ) { info ->
                    AlbumCard(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                        albumArtUri = info.albumUri,
                        title = info.title,
                        trackCount = info.trackCount
                    )
                }
            }
        }
    }
}