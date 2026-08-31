package com.autoclicker.data

import kotlinx.coroutines.flow.Flow

class ClickerRepository(private val dao: ClickerDao) {
    val allProfiles: Flow<List<ClickerProfile>> = dao.getAllProfiles()

    suspend fun insert(profile: ClickerProfile): Long = dao.insert(profile)
    suspend fun update(profile: ClickerProfile) = dao.update(profile)
    suspend fun delete(profile: ClickerProfile) = dao.delete(profile)
    suspend fun getById(id: Long): ClickerProfile? = dao.getProfileById(id)
}
