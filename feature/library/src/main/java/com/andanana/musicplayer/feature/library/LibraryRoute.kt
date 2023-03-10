package com.andanana.musicplayer.feature.library

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.andanana.musicplayer.core.designsystem.component.TabRowAndPager
import com.andanana.musicplayer.core.designsystem.theme.MusicPlayerTheme

enum class LibraryPage(
    @StringRes val titleResId: Int
) {
    PLAY_LIST(R.string.play_list_page_title),
    COLLECTION(R.string.collection_page_title),
}

@Composable
fun LibraryRoute(
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by libraryViewModel.uiState.collectAsState()
    LibraryScreen(
        uiState = uiState
    )
}

@Composable
private fun LibraryScreen(
    modifier: Modifier = Modifier,
    uiState: LibraryUiState
) {
    when (uiState) {
        LibraryUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        is LibraryUiState.Ready -> {
            LibraryContent(
                modifier = modifier
            )
        }
    }
}

@Composable
private fun LibraryContent(
    modifier: Modifier = Modifier
) {
    TabRowAndPager(
        modifier = modifier.fillMaxSize(),
        items = LibraryPage.values().toList(),
        tabRowContent = { page ->
            Text(
                modifier = modifier.padding(vertical = 10.dp),
                text = stringResource(id = page.titleResId)
            )
        },
        pagerContent = { page ->
            when (page) {
                LibraryPage.PLAY_LIST -> {
                    PlayListScreen(modifier = Modifier.fillMaxSize())
                }
                LibraryPage.COLLECTION -> {

                }
            }
        }
    )
}

@Preview
@Composable
private fun LibraryContentPreview() {
    MusicPlayerTheme {
        LibraryContent()
    }
}
