package com.autoclicker.viewmodel

import android.app.Application
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autoclicker.data.ClickerDatabase
import com.autoclicker.data.ClickerProfile
import com.autoclicker.data.ClickerRepository
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
    }

    fun delete(profile: ClickerProfile) = viewModelScope.launch {
        repo.delete(profile)
    }

    fun reorderProfiles(reordered: List<ClickerProfile>) = viewModelScope.launch {
        reordered.forEachIndexed { index, profile ->
            if (profile.sortOrder != index) {
                repo.update(profile.copy(sortOrder = index))
            }
        }
    }
}
