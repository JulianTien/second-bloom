package com.scf.secondbloom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clerk.api.Clerk
import com.clerk.ui.auth.AuthView
import com.scf.secondbloom.auth.SecondBloomClerkConfig
import com.scf.secondbloom.ui.i18n.LocalAppLanguage
import com.scf.secondbloom.ui.i18n.localized

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val user by Clerk.userFlow.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(user?.id) {
        if (user != null) {
            onDismiss()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = localized(language, "Log in", "登录"))
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = localized(language, "Back", "返回")
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (SecondBloomClerkConfig.isConfigured) {
            AuthView(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = localized(
                        language,
                        "Clerk is not configured yet.",
                        "Clerk 还未配置。"
                    ),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = localized(
                        language,
                        "Add a publishable key to enable email code and Google login.",
                        "配置 publishable key 后即可启用邮箱验证码和 Google 登录。"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
