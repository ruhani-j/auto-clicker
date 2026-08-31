package com.autoclicker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class ClickTypeConverters {
    @TypeConverter
    fun fromClickType(value: ClickType): String = value.name

    @TypeConverter
    fun toClickType(value: String): ClickType = ClickType.valueOf(value)
}

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE clicker_profiles_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                positionX INTEGER NOT NULL,
                positionY INTEGER NOT NULL,
                clickType TEXT NOT NULL,
                holdDurationMs INTEGER NOT NULL,
                isInfinite INTEGER NOT NULL,
                clickCount INTEGER NOT NULL,
                intervalMs INTEGER NOT NULL,
                jitterIntervalMs INTEGER NOT NULL,
                jitterPositionPx INTEGER NOT NULL,
                startDelayMs INTEGER NOT NULL,
                sortOrder INTEGER NOT NULL
            )
        """.trimIndent())
        database.execSQL("""
            INSERT INTO clicker_profiles_new
            SELECT id, name, positionX, positionY, clickType, holdDurationMs, isInfinite,
                   clickCount, intervalMs, jitterIntervalMs, jitterPositionPx, startDelayMs, sortOrder
            FROM clicker_profiles
        """.trimIndent())
        database.execSQL("DROP TABLE clicker_profiles")
        database.execSQL("ALTER TABLE clicker_profiles_new RENAME TO clicker_profiles")
    }
}

@Database(entities = [ClickerProfile::class], version = 2, exportSchema = false)
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
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
