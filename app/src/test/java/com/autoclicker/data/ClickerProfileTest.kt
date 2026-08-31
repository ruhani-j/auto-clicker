package com.autoclicker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClickerProfileTest {

    @Test
    fun defaultProfileHasSensibleValues() {
        val profile = ClickerProfile()
        assertEquals(0L, profile.id)
        assertEquals("New Clicker", profile.name)
        assertEquals(ClickType.SINGLE_TAP, profile.clickType)
        assertEquals(1000L, profile.intervalMs)
        assertTrue(profile.isInfinite)
        assertEquals(0, profile.sortOrder)
        assertEquals(0L, profile.jitterIntervalMs)
        assertEquals(0, profile.jitterPositionPx)
        assertEquals(0L, profile.startDelayMs)
    }

    @Test
    fun copyOverridesOnlySpecifiedFields() {
        val profile = ClickerProfile(name = "Original", intervalMs = 500L, sortOrder = 1)
        val updated = profile.copy(name = "Updated", intervalMs = 250L)
        assertEquals("Updated", updated.name)
        assertEquals(250L, updated.intervalMs)
        assertEquals(1, updated.sortOrder)
        assertEquals(profile.id, updated.id)
    }

    @Test
    fun clickTypesAreDistinct() {
        assertNotEquals(ClickType.SINGLE_TAP, ClickType.PRESS_AND_HOLD)
    }

    @Test
    fun holdDurationDefaultIsReasonable() {
        val profile = ClickerProfile()
        assertTrue("Hold duration should be positive", profile.holdDurationMs > 0)
    }

    @Test
    fun defaultPositionIsOnScreen() {
        val profile = ClickerProfile()
        assertTrue(profile.positionX > 0)
        assertTrue(profile.positionY > 0)
    }
}
