package com.scf.secondbloom.data.auth

import android.content.Context
import com.clerk.api.Clerk
import com.clerk.api.network.serialization.ClerkResult
import com.clerk.api.session.GetTokenOptions
import com.scf.secondbloom.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AuthUserSummary(
    val userId: String,
    val displayName: String,
    val primaryEmailAddress: String?,
    val avatarUrl: String?
)

data class SecondBloomAuthState(
    val isClerkConfigured: Boolean,
    val isClerkInitialized: Boolean,
    val isSignedIn: Boolean,
    val user: AuthUserSummary? = null,
    val initializationErrorMessage: String? = null
) {
    val shouldShowLoginEntry: Boolean
        get() = isClerkConfigured && !isSignedIn
}

interface AuthTokenProvider {
    suspend fun getBearerToken(): String?

    fun currentUserId(): String?
}

class SecondBloomAuthManager private constructor(
    context: Context
) : AuthTokenProvider {

    private val appContext = context.applicationContext
    private val publishableKey = BuildConfig.CLERK_PUBLISHABLE_KEY.trim()
    private val isConfigured = publishableKey.isNotBlank()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val state: StateFlow<SecondBloomAuthState> = if (!isConfigured) {
        MutableStateFlow(
            SecondBloomAuthState(
                isClerkConfigured = false,
                isClerkInitialized = false,
                isSignedIn = false
            )
        )
    } else {
        combine(
            Clerk.isInitialized,
            Clerk.userFlow,
            Clerk.initializationError
        ) { isInitialized, user, error ->
            SecondBloomAuthState(
                isClerkConfigured = true,
                isClerkInitialized = isInitialized,
                isSignedIn = user != null,
                user = user?.toSummary(),
                initializationErrorMessage = error?.message?.takeIf { it.isNotBlank() }
            )
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = SecondBloomAuthState(
                isClerkConfigured = true,
                isClerkInitialized = false,
                isSignedIn = false
            )
        )
    }

    override suspend fun getBearerToken(): String? {
        if (!isConfigured) {
            return null
        }

        // Keep the default Clerk session token path available when no template is configured.
        // The backend validates that token against Clerk JWKS and issuer directly.
        val result = Clerk.auth.getToken(
            BuildConfig.CLERK_JWT_TEMPLATE.trim().takeIf { it.isNotBlank() }?.let { template ->
                GetTokenOptions(template = template)
            } ?: GetTokenOptions()
        )

        return (result as? ClerkResult.Success<String>)?.value?.takeIf { it.isNotBlank() }
    }

    override fun currentUserId(): String? = if (isConfigured) Clerk.user?.id else null

    suspend fun signOut(): Boolean {
        if (!isConfigured) {
            return false
        }
        return Clerk.auth.signOut() is ClerkResult.Success
    }

    fun isConfigured(): Boolean = isConfigured

    companion object {
        @Volatile
        private var instance: SecondBloomAuthManager? = null

        fun getInstance(context: Context): SecondBloomAuthManager =
            instance ?: synchronized(this) {
                instance ?: SecondBloomAuthManager(context).also { instance = it }
            }
    }
}

private fun com.clerk.api.user.User.toSummary(): AuthUserSummary {
    val displayName = listOfNotNull(firstName, lastName)
        .joinToString(separator = " ")
        .trim()
        .ifBlank {
            username?.takeIf { it.isNotBlank() }
                ?: primaryEmailAddress?.emailAddress?.takeIf { it.isNotBlank() }
                ?: "Second Bloom"
        }

    return AuthUserSummary(
        userId = id,
        displayName = displayName,
        primaryEmailAddress = primaryEmailAddress?.emailAddress,
        avatarUrl = imageUrl
    )
}
