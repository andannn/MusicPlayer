package com.andanana.musicplayer.core.data.repository

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.andanana.musicplayer.core.data.util.CrQueryParameter
import com.andanana.musicplayer.core.data.util.CrQueryUtil
import com.andanana.musicplayer.core.model.AlbumInfo
import com.andanana.musicplayer.core.model.ArtistInfo
import com.andanana.musicplayer.core.model.MusicInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "LocalMusicRepositoryImp"

private val MusicInfoProjection = listOf(
    MediaStore.Audio.Media._ID,
    MediaStore.Audio.Media.TITLE,
    MediaStore.Audio.Media.DURATION,
    MediaStore.Audio.Media.DATE_MODIFIED,
    MediaStore.Audio.Media.SIZE,
    MediaStore.Audio.Media.MIME_TYPE,
    MediaStore.Audio.Media.DATA,
    MediaStore.Audio.Media.ALBUM,
    MediaStore.Audio.Media.ARTIST,
    MediaStore.Audio.Media.ALBUM_ID,
).toTypedArray()

private val ArtistInfoProjection = listOf(
    MediaStore.Audio.Artists._ID,
    MediaStore.Audio.Artists.ARTIST,
    MediaStore.Audio.Artists.NUMBER_OF_TRACKS
).toTypedArray()

private val AlbumInfoProjection = listOf(
    MediaStore.Audio.Albums._ID,
    MediaStore.Audio.Albums.ALBUM,
    MediaStore.Audio.Albums.NUMBER_OF_SONGS
).toTypedArray()

private const val MimeTypeLimitation = "(${MediaStore.Audio.Media.MIME_TYPE} in (?,?,?))"
private val MimeTypeSelectionArg = listOf(
    "audio/x-wav",
    "audio/mp4",
    "audio/flac"
).toTypedArray()

class LocalMusicRepositoryImpl @Inject constructor(
    private val app: Application
) : LocalMusicRepository {

    override suspend fun getAllMusicInfo() = withContext(Dispatchers.IO) {
        queryMusicInfo {
            projection = MusicInfoProjection
            where = MimeTypeLimitation
            selectionArgs = MimeTypeSelectionArg
        }
    }

    override suspend fun getMusicInfoByAlbumId(id: Long) = withContext(Dispatchers.IO) {
        queryMusicInfo {
            val albumLimitation = "(${MediaStore.Audio.Media.ALBUM_ID} like ?)"
            val albumSelectArgs = listOf(id.toString())

            projection = MusicInfoProjection
            where = "$MimeTypeLimitation AND $albumLimitation"
            selectionArgs = MimeTypeSelectionArg + albumSelectArgs
        }
    }

    override suspend fun getMusicInfoByArtistId(id: Int) = withContext(Dispatchers.IO) {
        queryMusicInfo {
            val artistLimitation = "(${MediaStore.Audio.Media.ARTIST_ID} like ?)"
            val artistSelectArgs = listOf(id.toString())

            projection = MusicInfoProjection
            where = "$MimeTypeLimitation AND $artistLimitation"
            selectionArgs = MimeTypeSelectionArg + artistSelectArgs
        }
    }

    override suspend fun getAllAlbumInfo() = withContext(Dispatchers.IO) {
        queryAlbumInfo {
            projection = AlbumInfoProjection
        }
    }

    override suspend fun getAllArtistInfo() = withContext(Dispatchers.IO) {
        queryArtistInfo {
            projection = ArtistInfoProjection
        }
    }

    private fun queryMusicInfo(paramAdjuster: CrQueryParameter.() -> Unit): List<MusicInfo> {
        val params = CrQueryParameter()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        params.paramAdjuster()
        return CrQueryUtil.query(app, uri, params)?.use { cursor ->
            parseMusicInfoCursor(cursor)
        } ?: emptyList()
    }

    private fun queryArtistInfo(paramAdjuster: CrQueryParameter.() -> Unit): List<ArtistInfo> {
        val params = CrQueryParameter()
        val uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        params.paramAdjuster()
        return CrQueryUtil.query(app, uri, params)?.use { cursor ->
            parseArtistInfoCursor(cursor)
        } ?: emptyList()
    }

    private fun queryAlbumInfo(paramAdjuster: CrQueryParameter.() -> Unit): List<AlbumInfo> {
        val params = CrQueryParameter()
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        params.paramAdjuster()
        return CrQueryUtil.query(app, uri, params)?.use { cursor ->
            parseAlbumInfoCursor(cursor)
        } ?: emptyList()
    }

    private fun parseMusicInfoCursor(cursor: Cursor): List<MusicInfo> {
        val itemList = mutableListOf<MusicInfo>()

        val idIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
        val titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
        val durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
        val dateModifiedIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
        val sizeIndex = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
        val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
        val dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
        val albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
        val artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
        val albumIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
        while (cursor.moveToNext()) {
            itemList.add(
                MusicInfo(
                    contentUri = Uri.withAppendedPath(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        cursor.getInt(idIndex).toString()
                    ),
                    title = cursor.getString(titleIndex),
                    duration = cursor.getInt(durationIndex),
                    modifiedDate = cursor.getLong(dateModifiedIndex),
                    size = cursor.getInt(sizeIndex),
                    mimeType = cursor.getString(mimeTypeIndex),
                    absolutePath = cursor.getString(dataIndex),
                    album = cursor.getString(albumIndex),
                    artist = cursor.getString(artistIndex),
                    albumUri = Uri.withAppendedPath(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        cursor.getLong(albumIdIndex).toString()
                    )
                ).also {
                    Log.d(TAG, "parseMusicInfoCursor: $it")
                }
            )
        }
        return itemList
    }

    private fun parseArtistInfoCursor(cursor: Cursor): List<ArtistInfo> {
        val itemList = mutableListOf<ArtistInfo>()

        val idIndex = cursor.getColumnIndex(MediaStore.Audio.Artists._ID)
        val artistIndex = cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)
        val numberOfTracksIndex = cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
        while (cursor.moveToNext()) {
            itemList.add(
                ArtistInfo(
                    artistId = cursor.getLong(idIndex),
                    name = cursor.getString(artistIndex),
                    artistCoverUri = getArtistCoverUriByName(cursor.getString(artistIndex)) ?: Uri.parse(""),
                    trackCount = cursor.getInt(numberOfTracksIndex)
                )
            )
        }
        return itemList
    }

    private fun parseAlbumInfoCursor(cursor: Cursor): List<AlbumInfo> {
        val itemList = mutableListOf<AlbumInfo>()

        val idIndex = cursor.getColumnIndex(MediaStore.Audio.Albums._ID)
        val albumIndex = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)
        val numberOfSongsIndex = cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
        while (cursor.moveToNext()) {
            itemList.add(
                AlbumInfo(
                    albumId = cursor.getLong(idIndex),
                    albumUri = Uri.withAppendedPath(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        cursor.getLong(idIndex).toString()
                    ),
                    title = cursor.getString(albumIndex),
                    trackCount = cursor.getInt(numberOfSongsIndex)
                )
            )
        }
        return itemList
    }

    private fun getArtistCoverUriByName(name: String): Uri? {
        val params = CrQueryParameter()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        params.apply {
            projection = listOf(MediaStore.Audio.Media.ALBUM_ID).toTypedArray()
            where = "(${MediaStore.Audio.Media.ARTIST} like ?)"
            selectionArgs = listOf(name).toTypedArray()
            limit = 1
        }
        return CrQueryUtil.query(app, uri, params)?.use { cursor ->
            if (cursor.count >= 1) {
                cursor.moveToFirst()
                val albumIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                Uri.withAppendedPath(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    cursor.getLong(albumIdIndex).toString()
                )
            } else {
                null
            }
        }
    }
}