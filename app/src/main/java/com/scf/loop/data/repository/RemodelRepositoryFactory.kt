package com.scf.loop.data.repository

import android.content.Context
import android.net.Uri
import com.scf.loop.BuildConfig
import com.scf.loop.data.remote.RealRemodelApi
import com.scf.loop.data.remote.mock.MockRemodelApi

object RemodelRepositoryFactory {
    fun create(context: Context): RemodelRepository {
        val api = if (BuildConfig.REMODEL_USE_REAL_API && BuildConfig.REMODEL_API_BASE_URL.isNotBlank()) {
            RealRemodelApi(
                baseUrl = BuildConfig.REMODEL_API_BASE_URL,
                openImageStream = { uri ->
                    context.contentResolver.openInputStream(Uri.parse(uri))
                }
            )
        } else {
            MockRemodelApi()
        }

        return DefaultRemodelRepository(api)
    }
}
