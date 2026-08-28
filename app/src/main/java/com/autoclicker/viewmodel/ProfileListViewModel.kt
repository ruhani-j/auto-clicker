package com.autoclicker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autoclicker.data.ClickerDatabase
import com.autoclicker.data.ClickerProfile
import com.autoclicker.data.ClickerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileListViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ClickerRepository(ClickerDatabase.getInstance(app).clickerDao())

    val profiles = repo.allProfiles.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addProfile() = viewModelScope.launch {
        repo.insert(ClickerProfile(name = "Clicker ${profiles.value.size + 1}"))
    }

    fun delete(profile: ClickerProfile) = viewModelScope.launch { repo.delete(profile) }

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
