package com.streamflow.app.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Profile(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val isKids: Boolean = false,
    val isMaster: Boolean = false
)

data class UserSession(
    val uid: String,
    val email: String,
    val displayName: String,
    val subscriptionTier: String = "Ultra 4K HDR",
    val profiles: List<Profile> = emptyList(),
    val activeProfileId: String = ""
)
