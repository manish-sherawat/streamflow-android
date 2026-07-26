package com.streamflow.app.data.repository

import com.streamflow.app.data.model.UserSession
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val userSession: StateFlow<UserSession?>
    val isAuthenticated: StateFlow<Boolean>
    val isDataSaverEnabled: StateFlow<Boolean>
    val isAutoPlayNextEnabled: StateFlow<Boolean>
    val preferredAudioLanguage: StateFlow<String>

    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun signup(email: String, password: String, displayName: String): Result<Unit>
    suspend fun loginAsGuest(): Result<Unit>
    suspend fun logout()
    suspend fun switchProfile(profileId: String)
    suspend fun toggleDataSaver(enabled: Boolean)
    suspend fun toggleAutoPlayNext(enabled: Boolean)
    suspend fun setPreferredAudioLanguage(lang: String)
}
