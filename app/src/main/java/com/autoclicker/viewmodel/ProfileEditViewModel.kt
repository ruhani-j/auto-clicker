package com.autoclicker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.autoclicker.data.ClickerDatabase
import com.autoclicker.data.ClickerProfile
import com.autoclicker.data.ClickerRepository
import com.autoclicker.data.ClickType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileEditViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ClickerRepository(ClickerDatabase.getInstance(app).clickerDao())

    private val _profile = MutableStateFlow(ClickerProfile())
    val profile = _profile.asStateFlow()

    fun load(id: Long) = viewModelScope.launch {
        repo.getById(id)?.let { _profile.value = it }
    }

    fun update(block: ClickerProfile.() -> ClickerProfile) {
        _profile.value = _profile.value.block()
    }

    fun save() = viewModelScope.launch {
        if (_profile.value.id == 0L) repo.insert(_profile.value)
        else repo.update(_profile.value)
    }
}
