package com.andanana.musicplayer.feature.library

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andanana.musicplayer.core.database.usecases.FAVORITE_PLAY_LIST_ID
import com.andanana.musicplayer.core.designsystem.component.AnimatedUpdateList
import com.andanana.musicplayer.core.designsystem.component.PlayListCard

@Composable
fun PlayListScreen(
    modifier: Modifier = Modifier,
    playListViewModel: PlayListViewModel = hiltViewModel()
) {
    val state by playListViewModel.playListPageUiState.collectAsState()
    PlayListPage(
        modifier = modifier,
        state = state
    )
}

@Composable
private fun PlayListPage(
    modifier: Modifier,
    state: PlayListPageUiState
) {
    when (state) {
        PlayListPageUiState.Loading -> {}
        is PlayListPageUiState.Ready -> {
            PlayListPageContent(
                modifier = modifier,
                itemList = state.infoList
            )
        }
    }
}

@Composable
private fun PlayListPageContent(
    modifier: Modifier = Modifier,
    itemList: List<PlayListItem>
) {
    AnimatedUpdateList(
        modifier = modifier,
        list = itemList,
        content = { item ->
            val coverImage = if (item.id == FAVORITE_PLAY_LIST_ID) {
                Icons.Rounded.FavoriteBorder
            } else {
                null
            }
            PlayListCard(
                modifier = Modifier.padding(vertical = 5.dp),
                title = item.name,
                coverImage = coverImage,
                trackCount = 0
            )
        }
    )
}
