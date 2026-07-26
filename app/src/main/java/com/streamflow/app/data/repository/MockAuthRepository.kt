package com.streamflow.app.data.repository

import com.streamflow.app.data.model.Profile
import com.streamflow.app.data.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAuthRepository @Inject constructor() : AuthRepository {

    private val defaultProfiles = listOf(
        Profile(id = "p1", name = "Alex", avatarUrl = "https://picsum.photos/seed/alex/150", isKids = false, isMaster = true),
        Profile(id = "p2", name = "Kids World", avatarUrl = "https://picsum.photos/seed/kids/150", isKids = true, isMaster = false),
        Profile(id = "p3", name = "Family", avatarUrl = "https://picsum.photos/seed/family/150", isKids = false, isMaster = false)
    )

    private val defaultUser = UserSession(
        uid = "user_demo_777",
        email = "demo@streamflow.app",
        displayName = "Alex Vance",
        subscriptionTier = "Ultra 4K HDR",
        profiles = defaultProfiles,
        activeProfileId = "p1"
    )

    private val _userSession = MutableStateFlow<UserSession?>(defaultUser)
    override val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(true)
    override val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isDataSaverEnabled = MutableStateFlow(false)
    override val isDataSaverEnabled: StateFlow<Boolean> = _isDataSaverEnabled.asStateFlow()

    private val _isAutoPlayNextEnabled = MutableStateFlow(true)
    override val isAutoPlayNextEnabled: StateFlow<Boolean> = _isAutoPlayNextEnabled.asStateFlow()

    private val _preferredAudioLanguage = MutableStateFlow("en")
    override val preferredAudioLanguage: StateFlow<String> = _preferredAudioLanguage.asStateFlow()

    override suspend fun login(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }
        val user = UserSession(
            uid = "user_${email.hashCode()}",
            email = email,
            displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
            subscriptionTier = "Ultra 4K HDR",
            profiles = defaultProfiles,
            activeProfileId = "p1"
        )
        _userSession.value = user
        _isAuthenticated.value = true
        return Result.success(Unit)
    }

    override suspend fun signup(email: String, password: String, displayName: String): Result<Unit> {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            return Result.failure(IllegalArgumentException("All fields are required"))
        }
        val user = UserSession(
            uid = "user_${email.hashCode()}",
            email = email,
            displayName = displayName,
            subscriptionTier = "Ultra 4K HDR",
            profiles = defaultProfiles,
            activeProfileId = "p1"
        )
        _userSession.value = user
        _isAuthenticated.value = true
        return Result.success(Unit)
    }

    override suspend fun loginAsGuest(): Result<Unit> {
        val guestUser = UserSession(
            uid = "user_guest_99",
            email = "guest@streamflow.app",
            displayName = "Guest User",
            subscriptionTier = "Standard HD",
            profiles = defaultProfiles.take(1),
            activeProfileId = "p1"
        )
        _userSession.value = guestUser
        _isAuthenticated.value = true
        return Result.success(Unit)
    }

    override suspend fun logout() {
        _userSession.value = null
        _isAuthenticated.value = false
    }

    override suspend fun switchProfile(profileId: String) {
        val current = _userSession.value ?: return
        if (current.profiles.any { it.id == profileId }) {
            _userSession.value = current.copy(activeProfileId = profileId)
        }
    }

    override suspend fun toggleDataSaver(enabled: Boolean) {
        _isDataSaverEnabled.value = enabled
    }

    override suspend fun toggleAutoPlayNext(enabled: Boolean) {
        _isAutoPlayNextEnabled.value = enabled
    }

    override suspend fun setPreferredAudioLanguage(lang: String) {
        _preferredAudioLanguage.value = lang
    }
}
