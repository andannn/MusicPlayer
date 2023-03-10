package com.andanana.musicplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.andanana.musicplayer.core.database.entity.Music
import com.andanana.musicplayer.core.database.entity.PlayListWithMusics
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreMusicEntities(
        entities: List<Music>
    )

    @Transaction
    @Query("SELECT * FROM play_list WHERE play_list_id = :playListId")
    fun getPlayListWithMusics(playListId: Long): Flow<PlayListWithMusics?>
}
