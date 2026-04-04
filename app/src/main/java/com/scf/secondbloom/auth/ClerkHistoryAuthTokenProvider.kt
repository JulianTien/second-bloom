package com.scf.secondbloom.auth

import com.clerk.api.Clerk
import com.clerk.api.network.serialization.ClerkResult
import com.clerk.api.session.GetTokenOptions
import com.scf.secondbloom.data.historysync.HistoryAuthTokenProvider

object ClerkHistoryAuthTokenProvider : HistoryAuthTokenProvider {
    override suspend fun currentAccessToken(): String? {
        if (!SecondBloomClerkConfig.isConfigured) {
            return null
        }

        // When no JWT template is configured, Clerk returns the default session token.
        // That is the token shape the backend expects for the v1 `/me/**` flow.
        val tokenOptions = SecondBloomClerkConfig.jwtTemplate
            .takeIf { it.isNotBlank() }
            ?.let(::GetTokenOptions)
        return when (val result = runCatching { Clerk.auth.getToken(tokenOptions) }.getOrNull()) {
            is ClerkResult.Success -> result.value.trim().takeIf { it.isNotBlank() }
            else -> null
        }
    }
}
