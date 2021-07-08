package com.dongdong.neonplayer.room.Entity

import androidx.room.TypeConverter
import java.util.*

class UserTypeConverter {
    @TypeConverter
    fun fromTimestamp(value : Long?) : Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimeStamp(date : Date?) : Long? = date?.time
}