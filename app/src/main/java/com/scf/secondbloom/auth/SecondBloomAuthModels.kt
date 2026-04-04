package com.scf.secondbloom.auth

data class SecondBloomAuthProfile(
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null
)

sealed interface SecondBloomAuthUiState {
    data object Unconfigured : SecondBloomAuthUiState
    data object Guest : SecondBloomAuthUiState
    data class SignedIn(
        val profile: SecondBloomAuthProfile
    ) : SecondBloomAuthUiState
}

fun buildAuthDisplayName(
    fullName: String?,
    firstName: String?,
    lastName: String?,
    fallback: String = "Clerk user"
): String {
    val trimmedFullName = fullName?.trim().orEmpty()
    if (trimmedFullName.isNotBlank()) {
        return trimmedFullName
    }

    val nameParts = listOfNotNull(
        firstName?.trim()?.takeIf { it.isNotBlank() },
        lastName?.trim()?.takeIf { it.isNotBlank() }
    )
    if (nameParts.isNotEmpty()) {
        return nameParts.joinToString(" ")
    }

    return fallback
}
