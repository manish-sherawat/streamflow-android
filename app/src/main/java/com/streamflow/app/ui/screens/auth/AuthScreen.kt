package com.streamflow.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.PlayCircleFilled
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamflow.app.ui.components.PrimaryButton
import com.streamflow.app.ui.components.SecondaryButton
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val displayName by viewModel.displayName.collectAsState()
    val isRegisterMode by viewModel.isRegisterMode.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    if (isAuthenticated) {
        onAuthSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0B1E), BgBase, BgBase)
                )
            )
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Radius.card)
                .background(BgElevated.copy(alpha = 0.9f))
                .border(1.dp, GlassBorder, Radius.card)
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.PlayCircleFilled,
                contentDescription = null,
                tint = AccentPrimary,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "StreamFlow",
                style = StreamFlowType.heroTitle,
                color = TextPrimary,
                modifier = Modifier.padding(top = Spacing.xs)
            )
            Text(
                text = if (isRegisterMode) "Create your StreamFlow account" else "Sign in to continue watching",
                style = StreamFlowType.body,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = Spacing.md)
            )

            if (isRegisterMode) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = viewModel::onDisplayNameChanged,
                    label = { Text("Display Name") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = GlassBorder,
                        focusedLabelColor = AccentPrimary,
                        unfocusedLabelColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
            }

            OutlinedTextField(
                value = email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPrimary,
                    unfocusedBorderColor = GlassBorder,
                    focusedLabelColor = AccentPrimary,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(Spacing.xs))

            OutlinedTextField(
                value = password,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPrimary,
                    unfocusedBorderColor = GlassBorder,
                    focusedLabelColor = AccentPrimary,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            errorMessage?.let { error ->
                Text(
                    text = error,
                    style = StreamFlowType.caption,
                    color = Color(0xFFFF5252),
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            if (isLoading) {
                CircularProgressIndicator(color = AccentPrimary)
            } else {
                PrimaryButton(
                    label = if (isRegisterMode) "Create Account" else "Sign In",
                    onClick = viewModel::submit,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                SecondaryButton(
                    label = "Continue as Demo Guest",
                    onClick = viewModel::loginAsGuest,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.clickable(onClick = viewModel::toggleAuthMode),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isRegisterMode) "Already have an account? " else "Don't have an account? ",
                    style = StreamFlowType.caption,
                    color = TextSecondary
                )
                Text(
                    text = if (isRegisterMode) "Sign In" else "Register Now",
                    style = StreamFlowType.caption,
                    color = AccentPrimary
                )
            }
        }
    }
}
