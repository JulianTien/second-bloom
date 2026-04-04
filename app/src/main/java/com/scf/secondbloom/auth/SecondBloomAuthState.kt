package com.scf.secondbloom.auth

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clerk.api.Clerk

@Composable
fun rememberSecondBloomAuthUiState(): SecondBloomAuthUiState {
    if (!SecondBloomClerkConfig.isConfigured) {
        return SecondBloomAuthUiState.Unconfigured
    }

    val user = Clerk.userFlow.collectAsStateWithLifecycle(initialValue = null).value
    return if (user == null) {
        SecondBloomAuthUiState.Guest
    } else {
        SecondBloomAuthUiState.SignedIn(
            profile = SecondBloomAuthProfile(
                userId = user.id,
                displayName = buildAuthDisplayName(
                    fullName = null,
                    firstName = user.firstName,
                    lastName = user.lastName
                ),
                avatarUrl = user.imageUrl
            )
        )
    }
}

suspend fun signOutCurrentUser() {
    if (SecondBloomClerkConfig.isConfigured) {
        Clerk.auth.signOut()
    }
}
