package com.example.gochat.data.database

import androidx.room.Transaction
import com.example.gochat.data.database.dao.UserInfoDao
import com.example.gochat.data.database.entity.UserInfo

@Transaction
suspend fun UserInfoDao.insertOrUpdate(userInfo: UserInfo) {
    val existingUser = getUserInfoById(userInfo.id)
    if (existingUser != null) {
        update(userInfo)
    } else {
        insert(userInfo)
    }
}