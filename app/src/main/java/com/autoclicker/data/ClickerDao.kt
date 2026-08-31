package com.autoclicker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClickerDao {
    @Query("SELECT * FROM clicker_profiles ORDER BY sortOrder ASC")
    fun getAllProfiles(): Flow<List<ClickerProfile>>

    @Query("SELECT * FROM clicker_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): ClickerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ClickerProfile): Long

    @Update
    suspend fun update(profile: ClickerProfile)

    @Delete
    suspend fun delete(profile: ClickerProfile)
}
