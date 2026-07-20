package com.streamflow.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamflow.app.data.model.Title
import com.streamflow.app.ui.components.AdminAddTitleDialog
import com.streamflow.app.ui.components.PosterCard
import com.streamflow.app.ui.components.PrimaryButton
import com.streamflow.app.ui.components.ProfileSelectionDialog
import com.streamflow.app.ui.components.SecondaryButton
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.GlassFill
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    onTitleClick: (Title) -> Unit,
    onOpenAuth: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val session by viewModel.userSession.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val isDataSaverEnabled by viewModel.isDataSaverEnabled.collectAsState()
    val downloadedTitles by viewModel.downloadedTitles.collectAsState()

    var showProfileDialog by remember { mutableStateOf(false) }
    var showAdminAddDialog by remember { mutableStateOf(false) }

    val activeProfile = session?.profiles?.find { it.id == session?.activeProfileId }
        ?: session?.profiles?.firstOrNull()

    val isAdmin = session?.email?.contains("admin", ignoreCase = true) == true ||
            session?.subscriptionTier?.contains("Admin") == true

    if (showProfileDialog && session != null) {
        ProfileSelectionDialog(
            profiles = session!!.profiles,
            activeProfileId = session!!.activeProfileId,
            onSelectProfile = viewModel::switchProfile,
            onDismiss = { showProfileDialog = false }
        )
    }

    if (showAdminAddDialog) {
        AdminAddTitleDialog(
            onAddTitle = viewModel::addTitle,
            onDismiss = { showAdminAddDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(horizontal = Spacing.md),
        contentPadding = PaddingValues(top = 56.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item {
            Text("Account & Settings", style = StreamFlowType.displayTitle)
        }

        if (!isAuthenticated || session == null) {
            // Unauthenticated Login Prompt Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Radius.card)
                        .background(BgElevated)
                        .border(1.dp, GlassBorder, Radius.card)
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        "Sign In to StreamFlow",
                        style = StreamFlowType.titleHeader,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                    Text(
                        "Access your personal watchlist, multi-profile switching, and admin management features.",
                        style = StreamFlowType.body,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.md)
                    )

                    PrimaryButton(
                        label = "Sign In / Register",
                        icon = Icons.AutoMirrored.Filled.Login,
                        onClick = onOpenAuth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // Authenticated User Profile Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Radius.card)
                        .background(BgElevated)
                        .border(1.dp, GlassBorder, Radius.card)
                        .padding(Spacing.lg)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AccentPrimary.copy(alpha = 0.2f))
                                .border(2.dp, AccentPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = Spacing.md)
                        ) {
                            Text(activeProfile?.name ?: "User Profile", style = StreamFlowType.titleHeader)
                            Text(session?.email ?: "user@streamflow.app", style = StreamFlowType.body, color = TextSecondary)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = Spacing.xs)
                                    .clip(Radius.pill)
                                    .background(GlassFill)
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(session?.subscriptionTier ?: "Ultra 4K HDR", style = StreamFlowType.caption, color = AccentPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    SecondaryButton(
                        label = "Switch Profile",
                        icon = Icons.Filled.SwapHoriz,
                        onClick = { showProfileDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Admin Management Panel
            if (isAdmin) {
                item {
                    Text("Admin Management", style = StreamFlowType.sectionHeader)
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Radius.card)
                            .background(BgElevated)
                            .border(1.dp, AccentPrimary.copy(alpha = 0.4f), Radius.card)
                            .padding(Spacing.lg)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(28.dp))
                            Column(modifier = Modifier.padding(start = Spacing.xs)) {
                                Text("Admin Catalog Control Panel", style = StreamFlowType.titleHeader)
                                Text("Manage live movies and shows in Cloud Firestore", style = StreamFlowType.caption, color = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        PrimaryButton(
                            label = "Add New Movie / Series",
                            icon = Icons.Filled.Add,
                            onClick = { showAdminAddDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        SecondaryButton(
                            label = "Seed Initial Cloud Catalog",
                            icon = Icons.Filled.CloudUpload,
                            onClick = { viewModel.seedCatalog() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Settings Section
        item {
            Text("Preferences", style = StreamFlowType.sectionHeader)
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Radius.card)
                    .background(BgElevated)
                    .border(1.dp, GlassBorder, Radius.card)
                    .padding(Spacing.md)
            ) {
                // Data Saver Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.NetworkCheck, contentDescription = null, tint = AccentPrimary)
                        Column(modifier = Modifier.padding(start = Spacing.md)) {
                            Text("Data Saver Mode", style = StreamFlowType.titleHeader)
                            Text(
                                "Caps video playback bitrate to 1.5 Mbps to reduce mobile data usage.",
                                style = StreamFlowType.caption,
                                color = TextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = isDataSaverEnabled,
                        onCheckedChange = viewModel::toggleDataSaver,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextPrimary,
                            checkedTrackColor = AccentPrimary
                        )
                    )
                }
            }
        }

        // Offline Downloads Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Offline Downloads", style = StreamFlowType.sectionHeader)
                Text("${downloadedTitles.size} Saved", style = StreamFlowType.caption, color = TextSecondary)
            }
        }

        item {
            if (downloadedTitles.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Radius.card)
                        .background(GlassFill)
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.DownloadDone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                    Text(
                        "No offline downloads yet.",
                        style = StreamFlowType.body,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = Spacing.xs)
                    )
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    items(downloadedTitles, key = { it.id }) { title ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PosterCard(
                                posterUrl = title.posterUrl,
                                titleLabel = title.name,
                                onClick = { onTitleClick(title) }
                            )
                            Row(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clickable { viewModel.removeDownload(title.id) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(14.dp))
                                Text("Remove", style = StreamFlowType.caption, color = TextSecondary, modifier = Modifier.padding(start = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        // Sign Out (if logged in)
        if (isAuthenticated && session != null) {
            item {
                Spacer(modifier = Modifier.height(Spacing.md))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Radius.pill)
                        .background(GlassFill)
                        .border(1.dp, GlassBorder, Radius.pill)
                        .clickable {
                            viewModel.logout()
                            onSignOut()
                        }
                        .padding(vertical = Spacing.md),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = TextPrimary)
                    Text("Sign Out", style = StreamFlowType.buttonLabel, color = TextPrimary, modifier = Modifier.padding(start = Spacing.xs))
                }
            }
        }
    }
}
