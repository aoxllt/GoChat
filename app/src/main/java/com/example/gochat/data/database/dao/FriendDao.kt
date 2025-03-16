package com.example.gochat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gochat.api.Friend

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(friends: List<Friend>)

    @Query("SELECT * FROM friends")
    suspend fun getAllFriends(): List<Friend>
}