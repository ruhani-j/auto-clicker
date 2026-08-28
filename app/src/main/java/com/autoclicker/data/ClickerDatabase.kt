package com.autoclicker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class ClickTypeConverters {
    @TypeConverter
    fun fromClickType(value: ClickType): String = value.name

    @TypeConverter
    fun toClickType(value: String): ClickType = ClickType.valueOf(value)
}

@Database(entities = [ClickerProfile::class], version = 1, exportSchema = false)
@TypeConverters(ClickTypeConverters::class)
abstract class ClickerDatabase : RoomDatabase() {
    abstract fun clickerDao(): ClickerDao

    companion object {
        @Volatile private var instance: ClickerDatabase? = null

        fun getInstance(context: Context): ClickerDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ClickerDatabase::class.java,
                    "clicker_db"
                ).build().also { instance = it }
            }
    }
}
