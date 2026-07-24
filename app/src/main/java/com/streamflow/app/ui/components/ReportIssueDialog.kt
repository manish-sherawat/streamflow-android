package com.streamflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.streamflow.app.ui.theme.BgCard
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

val reportIssueTypes = listOf(
    "🎥 Video not playing / Black screen",
    "🔊 Audio issue / Bad sound sync",
    "🎬 Wrong episode / Wrong video stream",
    "💬 Subtitles missing / Out of sync",
    "⬇️ Broken download link",
    "⚙️ Other / Custom Issue"
)

@Composable
fun ReportIssueDialog(
    titleName: String,
    isSubmitting: Boolean = false,
    onSubmit: (issueType: String, details: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedIssue by remember { mutableStateOf(reportIssueTypes.first()) }
    var customDetails by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(BgElevated)
                .border(1.dp, GlassBorder, MaterialTheme.shapes.large)
                .padding(Spacing.lg)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = "Report Issue",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Report an Issue",
                            style = StreamFlowType.sheetHeader.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = titleName,
                            style = StreamFlowType.caption,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BgCard)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))

            Text(
                text = "SELECT ISSUE TYPE",
                style = StreamFlowType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = TextSecondary
            )

            Spacer(Modifier.height(Spacing.xs))

            // Issue Options List
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                reportIssueTypes.forEach { issue ->
                    val isSelected = issue == selectedIssue
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else BgCard)
                            .border(
                                width = if (isSelected) 1.dp else 0.5.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.outline else GlassBorder,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable { selectedIssue = issue }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = issue,
                            style = StreamFlowType.body.copy(
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // Optional Additional Details Text Field
            Text(
                text = "ADDITIONAL DETAILS (OPTIONAL)",
                style = StreamFlowType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = TextSecondary
            )

            Spacer(Modifier.height(Spacing.xs))

            OutlinedTextField(
                value = customDetails,
                onValueChange = { customDetails = it },
                placeholder = {
                    Text(
                        text = "Describe what went wrong (e.g. at 05:20 audio cuts out...)",
                        style = StreamFlowType.caption,
                        color = TextSecondary.copy(alpha = 0.6f)
                    )
                },
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp),
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = BgCard,
                    unfocusedContainerColor = BgCard
                )
            )

            Spacer(Modifier.height(Spacing.lg))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryButton(
                    label = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(enabled = !isSubmitting) {
                            onSubmit(selectedIssue, customDetails.trim())
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Submit Report",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
