package com.example.gochat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.gochat.data.database.entity.User
import com.example.gochat.data.database.entity.UserInfo
import com.example.gochat.data.database.entity.enums.UserStatus
// 关联类
import androidx.room.Embedded
import androidx.room.Relation
@Dao
interface UserDao {
    // 插入操作
    @Insert
    suspend fun insertUser(user: User): Long // 返回插入的 ID

    @Insert
    suspend fun insertUserInfo(userInfo: UserInfo)

    // 查询操作
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM user_info")
    suspend fun getAllUserInfo(): List<UserInfo>

    @Query("SELECT * FROM users WHERE status = :status LIMIT 1")
    suspend fun getActiveUser(status: UserStatus = UserStatus.ACTIVE): User?

    @Query("SELECT * FROM user_info WHERE id = :userId")
    suspend fun getUserInfoById(userId: Int): UserInfo?

    // 删除操作
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Int)

    @Query("DELETE FROM users")
    suspend fun clearUsers()

    @Query("DELETE FROM user_info")
    suspend fun clearUserInfo()

    // 更新操作
    @Update
    suspend fun updateUser(user: User)

    @Update
    suspend fun updateUserInfo(userInfo: UserInfo)

    // 事务操作
    @Transaction
    suspend fun clearAll() {
        clearUserInfo() // 先清除从表
        clearUsers()    // 再清除主表
    }

    @Transaction
    @Query("SELECT * FROM users")
    suspend fun getUsersWithInfo(): List<UserWithInfo>
}



data class UserWithInfo(
    @Embedded
    val user: User,

    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val userInfo: UserInfo?
)