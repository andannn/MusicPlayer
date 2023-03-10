package com.andanana.musicplayer.core.player.repository

import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PlayerRepositoryImpl"

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    private val player: Player
) : PlayerRepository {

    private val playerStateFlow = MutableStateFlow<PlayerState>(PlayerState.Idle)
    private val playingMediaItemStateFlow = MutableStateFlow<MediaItem?>(null)

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val state = when (playbackState) {
                Player.STATE_IDLE -> PlayerState.Idle
                Player.STATE_BUFFERING -> PlayerState.Buffering
                Player.STATE_READY -> {
                    if (player.isPlaying) {
                        PlayerState.Playing(player.currentPosition)
                    } else {
                        PlayerState.Paused(player.currentPosition)
                    }
                }
                Player.STATE_ENDED -> PlayerState.PlayBackEnd
                else -> error("Impossible")
            }

            playerStateFlow.update { state }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            Log.d(TAG, "onPlayWhenReadyChanged: $playWhenReady  reason $reason")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                playerStateFlow.value = PlayerState.Playing(player.currentPosition)
            } else {
                val suppressed =
                    player.playbackSuppressionReason != Player.PLAYBACK_SUPPRESSION_REASON_NONE
                val playerError = player.playerError != null
                if (player.playbackState == Player.STATE_READY && !suppressed && !playerError) {
                    playerStateFlow.value = PlayerState.Paused(player.currentPosition)
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
// TODO: Define exception type and send back.
            playerStateFlow.value = PlayerState.Error(error)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            Log.d(TAG, "onMediaItemTransition: $mediaItem  reason $reason")
            playingMediaItemStateFlow.value = mediaItem
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            playerStateFlow.getAndUpdate { state ->
                when (state) {
                    is PlayerState.Playing -> {
                        state.copy(currentPositionMs = newPosition.contentPositionMs)
                    }
                    is PlayerState.Paused -> {
                        state.copy(currentPositionMs = newPosition.contentPositionMs)
                    }
                    else -> state
                }
            }
        }
    }

    init {
        Log.d(TAG, "creatae: ")
        player.prepare()
        player.playWhenReady = true
        player.addListener(playerListener)
    }

    override val currentPositionMs: Long
        get() = player.currentPosition

    override val playerState: PlayerState
        get() = playerStateFlow.value

    override fun observePlayerState(): Flow<PlayerState> = playerStateFlow

    override fun observePlayingUri(): Flow<Uri?> =
        playingMediaItemStateFlow.map {
            it?.localConfiguration?.uri
        }

    override fun setPlayList(mediaItems: List<MediaItem>) {
        player.setMediaItems(mediaItems)
    }

    override fun seekToMediaIndex(index: Int) {
        player.seekToDefaultPosition(index)
    }

    override fun play() {
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun next() {
        player.seekToNextMediaItem()
        player.play()
    }

    override fun seekTo(time: Int) {
        player.seekTo(time.toLong())
    }

    override fun previous() {
        player.seekToPreviousMediaItem()
        player.play()
    }
}
