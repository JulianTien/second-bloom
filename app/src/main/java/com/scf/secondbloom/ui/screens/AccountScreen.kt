package com.scf.secondbloom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clerk.ui.userprofile.UserProfileView
import com.scf.secondbloom.auth.SecondBloomAuthUiState
import com.scf.secondbloom.ui.i18n.LocalAppLanguage
import com.scf.secondbloom.ui.i18n.localized

@Composable
fun AccountScreen(
    authState: SecondBloomAuthUiState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current

    if (authState is SecondBloomAuthUiState.SignedIn) {
        UserProfileView(onDismiss = onDismiss)
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = localized(language, "No account is active yet.", "当前还没有登录账号。"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = localized(
                    language,
                    "Sign in first to manage your profile, sessions, and connected accounts.",
                    "请先登录，再管理账号资料、会话和关联登录方式。"
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
