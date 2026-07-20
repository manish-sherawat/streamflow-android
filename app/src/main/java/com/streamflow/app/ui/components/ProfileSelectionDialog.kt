package com.streamflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.streamflow.app.data.model.Profile
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

@Composable
fun ProfileSelectionDialog(
    profiles: List<Profile>,
    activeProfileId: String,
    onSelectProfile: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Radius.card)
                .background(BgElevated)
                .border(1.dp, GlassBorder, Radius.card)
                .padding(Spacing.lg)
        ) {
            Text("Switch Profile", style = StreamFlowType.sheetHeader, color = TextPrimary)
            Text(
                "Choose who is watching StreamFlow",
                style = StreamFlowType.body,
                color = TextSecondary,
                modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.md)
            )

            profiles.forEach { profile ->
                val isSelected = profile.id == activeProfileId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.xs)
                        .clip(Radius.secondaryButton)
                        .background(if (isSelected) AccentPrimary.copy(alpha = 0.15f) else BgElevated)
                        .border(
                            1.dp,
                            if (isSelected) AccentPrimary else GlassBorder,
                            Radius.secondaryButton
                        )
                        .clickable {
                            onSelectProfile(profile.id)
                            onDismiss()
                        }
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoxAvatar(isKids = profile.isKids)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = Spacing.md)
                    ) {
                        Text(profile.name, style = StreamFlowType.titleHeader, color = TextPrimary)
                        Text(
                            if (profile.isKids) "Kids Profile (Filtered content)" else if (profile.isMaster) "Master Profile" else "Standard Profile",
                            style = StreamFlowType.caption,
                            color = TextSecondary
                        )
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Active Profile",
                            tint = AccentPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))
            SecondaryButton(
                label = "Close",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BoxAvatar(isKids: Boolean) {
    Column(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isKids) AccentPrimary.copy(alpha = 0.2f) else GlassBorder),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isKids) Icons.Filled.ChildCare else Icons.Filled.Person,
            contentDescription = null,
            tint = TextPrimary
        )
    }
}
