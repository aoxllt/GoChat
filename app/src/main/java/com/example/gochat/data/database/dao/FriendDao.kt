package com.example.gochat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gochat.data.database.entity.Friend

@Dao
interface FriendDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<Friend>)

    @Query("SELECT * FROM friends WHERE owner_id = :ownerId")
    suspend fun getFriendsByOwnerId(ownerId: Int): List<Friend>

    @Query("DELETE FROM friends WHERE owner_id = :ownerId")
    suspend fun deleteFriendsByOwnerId(ownerId: Int)
}