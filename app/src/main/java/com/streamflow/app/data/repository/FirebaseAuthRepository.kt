package com.streamflow.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.streamflow.app.data.model.Profile
import com.streamflow.app.data.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor() : AuthRepository {

    private val firebaseAuth: FirebaseAuth? by lazy {
        runCatching { FirebaseAuth.getInstance() }.getOrNull()
    }
    private val firestore: FirebaseFirestore? by lazy {
        runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }

    private val defaultProfiles = listOf(
        Profile(id = "p1", name = "Main Profile", avatarUrl = "https://picsum.photos/seed/alex/150", isKids = false, isMaster = true),
        Profile(id = "p2", name = "Kids World", avatarUrl = "https://picsum.photos/seed/kids/150", isKids = true, isMaster = false)
    )

    private val _userSession = MutableStateFlow<UserSession?>(null)
    override val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    override val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isDataSaverEnabled = MutableStateFlow(false)
    override val isDataSaverEnabled: StateFlow<Boolean> = _isDataSaverEnabled.asStateFlow()

    init {
        try {
            firebaseAuth?.addAuthStateListener { auth ->
                val user = auth.currentUser
                if (user != null) {
                    val email = user.email ?: "user@streamflow.app"
                    val isAdmin = email.contains("admin", ignoreCase = true) || email.endsWith("@streamflow.app")
                    _userSession.value = UserSession(
                        uid = user.uid,
                        email = email,
                        displayName = user.displayName?.takeIf { it.isNotBlank() }
                            ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        subscriptionTier = if (isAdmin) "Admin / Ultra 4K HDR" else "Ultra 4K HDR",
                        profiles = defaultProfiles,
                        activeProfileId = "p1"
                    )
                    _isAuthenticated.value = true
                } else {
                    _userSession.value = null
                    _isAuthenticated.value = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        return runCatching {
            val auth = firebaseAuth ?: throw IllegalStateException("Firebase Auth not initialized")
            auth.signInWithEmailAndPassword(email, password).await()
            Unit
        }
    }

    override suspend fun signup(email: String, password: String, displayName: String): Result<Unit> {
        return runCatching {
            val auth = firebaseAuth ?: throw IllegalStateException("Firebase Auth not initialized")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                val userDoc = mapOf(
                    "uid" to user.uid,
                    "email" to email,
                    "displayName" to displayName,
                    "isAdmin" to email.contains("admin", ignoreCase = true)
                )
                firestore?.collection("users")?.document(user.uid)?.set(userDoc)?.await()
            }
            Unit
        }
    }

    override suspend fun loginAsGuest(): Result<Unit> {
        val guestSession = UserSession(
            uid = "guest_user_demo",
            email = "guest@streamflow.app",
            displayName = "Demo Guest User",
            subscriptionTier = "Standard HD",
            profiles = defaultProfiles.take(1),
            activeProfileId = "p1"
        )
        _userSession.value = guestSession
        _isAuthenticated.value = true
        return Result.success(Unit)
    }

    override suspend fun logout() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
}
