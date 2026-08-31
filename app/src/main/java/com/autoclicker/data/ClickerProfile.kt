package com.autoclicker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ClickType { SINGLE_TAP, PRESS_AND_HOLD }

@Entity(tableName = "clicker_profiles")
data class ClickerProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "New Clicker",
    val positionX: Int = 500,
    val positionY: Int = 500,
    val clickType: ClickType = ClickType.SINGLE_TAP,
    val holdDurationMs: Long = 500,
    val isInfinite: Boolean = true,
    val clickCount: Int = 10,
    val intervalMs: Long = 1000,
    val jitterIntervalMs: Long = 0,
    val jitterPositionPx: Int = 0,
    val startDelayMs: Long = 0,
    val sortOrder: Int = 0
)
