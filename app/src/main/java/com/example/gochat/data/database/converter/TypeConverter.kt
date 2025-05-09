package com.example.gochat.data.database

import androidx.room.TypeConverter
import com.example.gochat.data.database.entity.enums.UserStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE // 格式如 "2025-03-15"

    @TypeConverter
    @JvmStatic
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    @JvmStatic
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it, formatter) }
    }

    @TypeConverter
    fun fromUserStatus(status: UserStatus): String = status.name

    @TypeConverter
    fun toUserStatus(value: String): UserStatus = enumValueOf(value)
}