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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.Dialog
import com.streamflow.app.ui.components.PrimaryButton
import com.streamflow.app.ui.components.SecondaryButton
import com.streamflow.app.ui.theme.BgElevated
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamflow.app.data.model.Title
import com.streamflow.app.ui.components.AdminAddTitleDialog
import com.streamflow.app.ui.components.PosterCard
import com.streamflow.app.ui.components.PrimaryButton
import com.streamflow.app.ui.components.ProfileSelectionDialog
import com.streamflow.app.ui.components.SecondaryButton
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.AccentStar
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.BgCard
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.Divider
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextMuted
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    onTitleClick: (Title) -> Unit,
    onOpenAuth: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val session               by viewModel.userSession.collectAsState()
    val isAuthenticated       by viewModel.isAuthenticated.collectAsState()
    val isDataSaverEnabled    by viewModel.isDataSaverEnabled.collectAsState()
    val isAutoPlayNextEnabled by viewModel.isAutoPlayNextEnabled.collectAsState()
    val updateState           by viewModel.updateState.collectAsState()
    val uriHandler         = LocalUriHandler.current

    var showProfileDialog  by remember { mutableStateOf(false) }
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
            .background(BgBase),
        contentPadding = PaddingValues(bottom = 110.dp)
    ) {
        // Page header with status bars safe space
        item {
            Text(
                "Profile",
                style = StreamFlowType.displayTitle,
                color = TextPrimary,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(
                        horizontal = Spacing.md,
                        vertical = Spacing.sm
                    )
            )
        }

        if (!isAuthenticated || session == null) {
            // ── Unauthenticated state ─────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md)
                        .clip(Radius.card)
                        .background(BgCard)
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(BgElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Text(
                        "Sign in to StreamFlow",
                        style = StreamFlowType.sectionHeader.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        "Access your watchlist, profiles, and settings.",
                        style = StreamFlowType.body,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = Spacing.lg)
                    )
                    PrimaryButton(
                        label = "Sign In",
                        icon = Icons.AutoMirrored.Filled.Login,
                        onClick = onOpenAuth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.md))
            }
        } else {
            // ── Authenticated user card ───────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md)
                        .clip(Radius.card)
                        .background(BgCard)
                        .padding(Spacing.md)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(BgElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = Spacing.md)
                        ) {
                            Text(
                                activeProfile?.name ?: "User Profile",
                                style = StreamFlowType.sectionHeader.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                session?.email ?: "user@streamflow.app",
                                style = StreamFlowType.caption,
                                color = TextSecondary
                            )
                            // Subscription tier chip
                            Text(
                                session?.subscriptionTier ?: "Ultra 4K",
                                style = StreamFlowType.caption.copy(fontWeight = FontWeight.SemiBold),
                                color = AccentStar,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .background(BgElevated, Radius.chip)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))
                    HorizontalDivider(color = Divider, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(Spacing.sm))

                    SettingsRow(
                        icon = Icons.Filled.SwapHoriz,
                        label = "Switch Profile",
                        onClick = { showProfileDialog = true }
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.md))
            }

            // ── Viewing Analytics & Watch Stats ──────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md)
                        .clip(Radius.card)
                        .background(BgCard)
                        .border(1.dp, GlassBorder, Radius.card)
                        .padding(Spacing.md)
                ) {
                    Text(
                        text = "Viewing Analytics",
                        style = StreamFlowType.sectionHeader.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "28.5h", style = StreamFlowType.sectionHeader.copy(fontWeight = FontWeight.Bold, color = AccentPrimary))
                            Text(text = "Watch Time", style = StreamFlowType.caption.copy(fontSize = 10.sp), color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Anime", style = StreamFlowType.sectionHeader.copy(fontWeight = FontWeight.Bold, color = AccentStar))
                            Text(text = "Top Genre", style = StreamFlowType.caption.copy(fontSize = 10.sp), color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🔥 5 Days", style = StreamFlowType.sectionHeader.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFF4500)))
                            Text(text = "Streak", style = StreamFlowType.caption.copy(fontSize = 10.sp), color = TextSecondary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.md))
            }

            // ── Admin Panel ───────────────────────────────────────────────────
            if (isAdmin) {
                item {
                    ProfileSectionLabel("Admin")
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md)
                            .clip(Radius.card)
                            .background(BgCard)
                    ) {
                        SettingsRow(
                            icon = Icons.Filled.Add,
                            label = "Add New Title",
                            sublabel = "Add movie or series to catalog",
                            onClick = { showAdminAddDialog = true }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            color = Divider,
                            thickness = 0.5.dp
                        )
                        SettingsRow(
                            icon = Icons.Filled.CloudUpload,
                            label = "Seed Cloud Catalog",
                            sublabel = "Upload initial data to Firestore",
                            onClick = { viewModel.seedCatalog() }
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.md))
                }
            }
        }

        // ── Preferences ───────────────────────────────────────────────────────
        item {
            ProfileSectionLabel("Preferences")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md)
                    .clip(Radius.card)
                    .background(BgCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.NetworkCheck,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = Spacing.md)
                    ) {
                        Text("Data Saver", style = StreamFlowType.body.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
                        Text("Cap playback to 1.5 Mbps", style = StreamFlowType.caption, color = TextSecondary)
                    }
                    Switch(
                        checked = isDataSaverEnabled,
                        onCheckedChange = viewModel::toggleDataSaver,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor  = BgBase,
                            checkedTrackColor  = TextPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = BgElevated
                        )
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(start = 52.dp),
                    color = Divider,
                    thickness = 0.5.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = Spacing.md)
                    ) {
                        Text("Auto-Play Next Episode", style = StreamFlowType.body.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
                        Text("Automatically play next episode on series end", style = StreamFlowType.caption, color = TextSecondary)
                    }
                    Switch(
                        checked = isAutoPlayNextEnabled,
                        onCheckedChange = viewModel::toggleAutoPlayNext,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor  = BgBase,
                            checkedTrackColor  = TextPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = BgElevated
                        )
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(start = 52.dp),
                    color = Divider,
                    thickness = 0.5.dp
                )

                SettingsRow(
                    icon = Icons.Filled.SystemUpdate,
                    label = "Check for App Updates",
                    sublabel = "Current version: v${com.streamflow.app.BuildConfig.VERSION_NAME}",
                    onClick = { viewModel.checkForUpdates() }
                )
            }
            Spacer(modifier = Modifier.height(Spacing.md))
        }

        // ── Sign Out ──────────────────────────────────────────────────────────
        if (isAuthenticated && session != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md)
                        .clip(Radius.card)
                        .background(BgCard)
                        .clickable {
                            viewModel.logout()
                            onSignOut()
                        }
                        .padding(horizontal = Spacing.md, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Sign Out",
                        style = StreamFlowType.body.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFFFF5252),
                        modifier = Modifier.padding(start = Spacing.md)
                    )
                }
            }
        }
    }

    if (updateState.isChecking) {
        Dialog(onDismissRequest = { }) {
            Box(
                modifier = Modifier
                    .clip(Radius.card)
                    .background(BgElevated)
                    .border(1.dp, GlassBorder, Radius.card)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = AccentPrimary, modifier = Modifier.size(24.dp))
                    Text("Checking GitHub for updates...", style = StreamFlowType.body, color = TextPrimary)
                }
            }
        }
    }

    if (updateState.hasUpdate) {
        Dialog(onDismissRequest = { viewModel.dismissUpdateState() }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Radius.card)
                    .background(BgElevated)
                    .border(1.dp, GlassBorder, Radius.card)
                    .padding(24.dp)
            ) {
                Icon(
                    Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    tint = AccentPrimary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Update Available! (${updateState.latestVersion})",
                    style = StreamFlowType.sheetHeader.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "A new version of StreamFlow is ready to install.",
                    style = StreamFlowType.body,
                    color = TextSecondary
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "What's New:",
                    style = StreamFlowType.caption.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = updateState.changelog,
                    style = StreamFlowType.caption,
                    color = TextSecondary,
                    maxLines = 4,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        label = "Later",
                        onClick = { viewModel.dismissUpdateState() },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        label = "Update Now",
                        icon = Icons.Filled.SystemUpdate,
                        onClick = {
                            uriHandler.openUri(updateState.downloadUrl)
                            viewModel.dismissUpdateState()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (updateState.isUpToDate) {
        Dialog(onDismissRequest = { viewModel.dismissUpdateState() }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Radius.card)
                    .background(BgElevated)
                    .border(1.dp, GlassBorder, Radius.card)
                    .padding(24.dp)
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF34C759),
                    modifier = Modifier.size(44.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "StreamFlow is Up to Date",
                    style = StreamFlowType.sheetHeader.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "You are running the latest release (v${com.streamflow.app.BuildConfig.VERSION_NAME}).",
                    style = StreamFlowType.body,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(18.dp))
                PrimaryButton(
                    label = "OK",
                    onClick = { viewModel.dismissUpdateState() }
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = StreamFlowType.caption.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp
        ),
        color = TextSecondary,
        modifier = Modifier.padding(
            horizontal = Spacing.md,
            vertical = Spacing.xs
        )
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    sublabel: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = Spacing.md)
        ) {
            Text(label, style = StreamFlowType.body.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
            if (sublabel != null) {
                Text(sublabel, style = StreamFlowType.caption, color = TextSecondary)
            }
        }
    }
}
