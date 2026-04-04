package com.scf.secondbloom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.clerk.api.Clerk
import com.scf.secondbloom.auth.SecondBloomClerkConfig
import com.scf.secondbloom.ui.MainScreen
import com.scf.secondbloom.ui.theme.SecondBloomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SecondBloomClerkConfig.isConfigured) {
            Clerk.auth.handle(intent.data)
        }
        enableEdgeToEdge()
        setContent {
            SecondBloomTheme {
                MainScreen()
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (SecondBloomClerkConfig.isConfigured) {
            Clerk.auth.handle(intent.data)
        }
    }
}
