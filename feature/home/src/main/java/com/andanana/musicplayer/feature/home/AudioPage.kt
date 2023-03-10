package com.andanana.musicplayer.feature.home

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andanana.musicplayer.core.designsystem.component.MusicCard
import com.andanana.musicplayer.core.model.MusicInfo

private const val TAG = "AudioPage"

@Composable
fun AudioPage(
    modifier: Modifier = Modifier,
    audioPageViewModel: AudioPageViewModel = hiltViewModel(),
    onPlayMusicInList: (List<MusicInfo>, Int) -> Unit,
    onShowMusicItemOption: (Uri) -> Unit
) {
    val state by audioPageViewModel.audioPageUiState.collectAsState()
    AudioPageContent(
        modifier = modifier,
        state = state,
        onAudioItemClick = onPlayMusicInList,
        onShowMusicItemOption = onShowMusicItemOption
    )
}

@Composable
private fun AudioPageContent(
    modifier: Modifier = Modifier,
    state: AudioPageUiState,
    onAudioItemClick: (List<MusicInfo>, Int) -> Unit,
    onShowMusicItemOption: (Uri) -> Unit
) {
    when (state) {
        AudioPageUiState.Loading -> {
        }
        is AudioPageUiState.Ready -> {
            val musicInfoList = state.infoList

            LazyColumn(
                modifier = modifier
            ) {
                items(
                    items = musicInfoList,
                    key = { it.contentUri }
                ) { info ->
                    MusicCard(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp),
                        albumArtUri = info.albumUri,
                        title = info.title,
                        artist = info.artist,
                        date = info.modifiedDate,
                        onMusicItemClick = {
                            onAudioItemClick(musicInfoList, musicInfoList.indexOf(info))
                        },
                        onOptionButtonClick = {
                            onShowMusicItemOption(info.contentUri)
                        }
                    )
                }
            }
        }
    }
}
