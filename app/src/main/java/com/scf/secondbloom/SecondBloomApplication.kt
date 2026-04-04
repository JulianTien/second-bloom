package com.scf.secondbloom

import android.app.Application
import com.clerk.api.Clerk
import com.clerk.api.ClerkConfigurationOptions

class SecondBloomApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val publishableKey = BuildConfig.CLERK_PUBLISHABLE_KEY.trim()
        if (publishableKey.isBlank()) {
            return
        }

        Clerk.initialize(
            this,
            publishableKey,
            ClerkConfigurationOptions(enableDebugMode = BuildConfig.DEBUG)
        )
    }
}
