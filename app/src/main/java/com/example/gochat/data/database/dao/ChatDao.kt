package com.example.gochat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gochat.data.database.entity.Chat

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<Chat>)

    @Query("SELECT * FROM chats WHERE user_id = :userId")
    suspend fun getChatsByUserId(userId: Int): List<Chat>

    @Query("DELETE FROM chats WHERE user_id = :userId")
    suspend fun deleteChatsByUserId(userId: Int)
}