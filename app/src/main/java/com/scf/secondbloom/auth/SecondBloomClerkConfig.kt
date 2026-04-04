package com.scf.secondbloom.auth

import com.scf.secondbloom.BuildConfig

object SecondBloomClerkConfig {
    val publishableKey: String
        get() = BuildConfig.CLERK_PUBLISHABLE_KEY.trim()

    val jwtTemplate: String
        get() = BuildConfig.CLERK_JWT_TEMPLATE.trim()

    val isConfigured: Boolean
        get() = publishableKey.isNotBlank()
}
