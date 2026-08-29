package com.autoclicker.viewmodel

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autoclicker.data.ClickerDatabase
import com.autoclicker.data.ClickerProfile
import com.autoclicker.data.ClickerRepository
import com.autoclicker.service.AutoClickerAccessibilityService
import com.autoclicker.service.OverlayService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileListViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ClickerRepository(ClickerDatabase.getInstance(app).clickerDao())
    private val prefs = app.getSharedPreferences("autoclicker_prefs", 0)

    val profiles = repo.allProfiles.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val flashEnabled = mutableStateOf(prefs.getBoolean("flash_enabled", true))

    fun toggleFlash() {
        flashEnabled.value = !flashEnabled.value
        OverlayService.flashEnabled.value = flashEnabled.value
        prefs.edit().putBoolean("flash_enabled", flashEnabled.value).apply()
    }

    init {
        OverlayService.flashEnabled.value = flashEnabled.value
        viewModelScope.launch {
            profiles.collect { profileList ->
                // Keep activeProfiles in sync with DB atomically so snapshotFlow fires once
                Snapshot.withMutableSnapshot {
                    OverlayService.activeProfiles.clear()
                    OverlayService.activeProfiles.addAll(profileList)
                }

                val hasPermissions = AutoClickerAccessibilityService.instance != null
                        && Settings.canDrawOverlays(app)

                if (profileList.isNotEmpty() && !OverlayService.isRunning.value && hasPermissions) {
                    app.startForegroundService(
                        Intent(app, OverlayService::class.java).apply {
                            action = OverlayService.ACTION_START
                        }
                    )
                }

                if (profileList.isEmpty() && OverlayService.isRunning.value) {
                    app.startService(
                        Intent(app, OverlayService::class.java).apply {
                            action = OverlayService.ACTION_STOP
                        }
                    )
                }
            }
        }
    }

    fun addProfile() = viewModelScope.launch {
        repo.insert(ClickerProfile(name = "Clicker ${profiles.value.size + 1}"))
        // profiles flow re-emits → init collect syncs activeProfiles and starts service if needed
    }

    fun delete(profile: ClickerProfile) = viewModelScope.launch {
        repo.delete(profile)
    }

    fun moveUp(profile: ClickerProfile) = viewModelScope.launch {
        val list = profiles.value.toMutableList()
        val idx = list.indexOfFirst { it.id == profile.id }
        if (idx <= 0) return@launch
        val prev = list[idx - 1]
        repo.update(profile.copy(sortOrder = prev.sortOrder))
        repo.update(prev.copy(sortOrder = profile.sortOrder))
    }

    fun moveDown(profile: ClickerProfile) = viewModelScope.launch {
        val list = profiles.value.toMutableList()
        val idx = list.indexOfFirst { it.id == profile.id }
        if (idx < 0 || idx >= list.size - 1) return@launch
        val next = list[idx + 1]
        repo.update(profile.copy(sortOrder = next.sortOrder))
        repo.update(next.copy(sortOrder = profile.sortOrder))
    }
}
