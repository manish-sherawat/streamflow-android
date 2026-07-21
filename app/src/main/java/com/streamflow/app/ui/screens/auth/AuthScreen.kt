package com.streamflow.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamflow.app.ui.components.AccentButton
import com.streamflow.app.ui.components.PrimaryButton
import com.streamflow.app.ui.components.SecondaryButton
import com.streamflow.app.ui.components.StreamFlowSpinner
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.BgInput
import com.streamflow.app.ui.theme.Divider
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextMuted
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val email          by viewModel.email.collectAsState()
    val password       by viewModel.password.collectAsState()
    val displayName    by viewModel.displayName.collectAsState()
    val isRegisterMode by viewModel.isRegisterMode.collectAsState()
    val errorMessage   by viewModel.errorMessage.collectAsState()
    val isLoading      by viewModel.isLoading.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    if (isAuthenticated) { onAuthSuccess() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Wordmark ─────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(Spacing.xl))
            Text(
                text = "STREAMFLOW",
                style = StreamFlowType.brandTitle.copy(
                    fontSize = 28.sp,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Black
                ),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isRegisterMode) "Create your account" else "Sign in to continue",
                style = StreamFlowType.body,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xl + Spacing.lg))

            // ── Fields ───────────────────────────────────────────────────────
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = BgInput,
                unfocusedContainerColor = BgInput,
                focusedBorderColor      = TextPrimary.copy(alpha = 0.8f),
                unfocusedBorderColor    = GlassBorder,
                focusedLabelColor       = TextSecondary,
                unfocusedLabelColor     = TextMuted,
                focusedTextColor        = TextPrimary,
                unfocusedTextColor      = TextPrimary,
                focusedLeadingIconColor = TextSecondary,
                unfocusedLeadingIconColor = TextMuted
            )

            if (isRegisterMode) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = viewModel::onDisplayNameChanged,
                    label = { Text("Display Name") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = Radius.input,
                    colors = fieldColors,
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
            }

            OutlinedTextField(
                value = email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = Radius.input,
                colors = fieldColors,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(Spacing.sm))

            OutlinedTextField(
                value = password,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = Radius.input,
                colors = fieldColors,
                singleLine = true
            )

            // Error message
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = error,
                    style = StreamFlowType.caption,
                    color = Color(0xFFFF5252),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // ── Actions ──────────────────────────────────────────────────────
            if (isLoading) {
                StreamFlowSpinner(size = 32.dp)
            } else {
                AccentButton(
                    label = if (isRegisterMode) "Create Account" else "Sign In",
                    onClick = viewModel::submit,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                // Guest link
                Text(
                    text = "Continue as Guest",
                    style = StreamFlowType.body,
                    color = TextSecondary,
                    modifier = Modifier
                        .clickable(onClick = viewModel::loginAsGuest)
                        .padding(vertical = Spacing.sm)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // ── Auth mode toggle ─────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable(onClick = viewModel::toggleAuthMode)
                    .padding(Spacing.xs)
            ) {
                Text(
                    text = if (isRegisterMode) "Already have an account? " else "Don't have an account? ",
                    style = StreamFlowType.caption,
                    color = TextSecondary
                )
                Text(
                    text = if (isRegisterMode) "Sign In" else "Register",
                    style = StreamFlowType.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
            }
        }
    }
}
