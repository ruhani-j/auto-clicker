package com.autoclicker.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClickerDaoTest {
    private lateinit var db: ClickerDatabase
    private lateinit var dao: ClickerDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ClickerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.clickerDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun insertAndRetrieveProfile() = runTest {
        val id = dao.insert(ClickerProfile(name = "Test Clicker", intervalMs = 500L, sortOrder = 0))
        val retrieved = dao.getProfileById(id)
        assertNotNull(retrieved)
        assertEquals("Test Clicker", retrieved!!.name)
        assertEquals(500L, retrieved.intervalMs)
    }

    @Test
    fun deleteProfileRemovesIt() = runTest {
        val id = dao.insert(ClickerProfile(name = "To Delete", sortOrder = 0))
        val profile = dao.getProfileById(id)!!
        dao.delete(profile)
        assertNull(dao.getProfileById(id))
    }

    @Test
    fun getAllProfilesReturnsSortedBySortOrder() = runTest {
        dao.insert(ClickerProfile(name = "Third", sortOrder = 2))
        dao.insert(ClickerProfile(name = "First", sortOrder = 0))
        dao.insert(ClickerProfile(name = "Second", sortOrder = 1))
        val profiles = dao.getAllProfiles().first()
        assertEquals(3, profiles.size)
        assertEquals("First", profiles[0].name)
        assertEquals("Second", profiles[1].name)
        assertEquals("Third", profiles[2].name)
    }

    @Test
    fun updateProfilePersistsChanges() = runTest {
        val id = dao.insert(ClickerProfile(name = "Original", intervalMs = 1000L, sortOrder = 0))
        val profile = dao.getProfileById(id)!!
        dao.update(profile.copy(name = "Updated", intervalMs = 2000L))
        val updated = dao.getProfileById(id)!!
        assertEquals("Updated", updated.name)
        assertEquals(2000L, updated.intervalMs)
    }

    @Test
    fun insertMultipleProfilesAndCountThem() = runTest {
        repeat(3) { i -> dao.insert(ClickerProfile(name = "Clicker $i", sortOrder = i)) }
        val profiles = dao.getAllProfiles().first()
        assertEquals(3, profiles.size)
    }
}
