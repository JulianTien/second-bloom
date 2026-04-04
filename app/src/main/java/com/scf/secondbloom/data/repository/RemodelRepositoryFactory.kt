package com.scf.secondbloom.data.repository

import android.content.Context
import android.net.Uri
import com.scf.secondbloom.BuildConfig
import com.scf.secondbloom.data.remote.RealRemodelApi
import com.scf.secondbloom.data.remote.RemodelApi
import com.scf.secondbloom.data.remote.mock.MockRemodelApi
import java.io.InputStream

object RemodelRepositoryFactory {
    fun create(context: Context): RemodelRepository {
        return DefaultRemodelRepository(
            createApi(
                useRealApi = BuildConfig.REMODEL_USE_REAL_API,
                baseUrl = BuildConfig.REMODEL_API_BASE_URL,
                openImageStream = { uri ->
                    context.contentResolver.openInputStream(Uri.parse(uri))
                }
            )
        )
    }

    internal fun createApi(
        useRealApi: Boolean,
        baseUrl: String,
        openImageStream: (String) -> InputStream?
    ): RemodelApi {
        val normalizedBaseUrl = baseUrl.trim()
        return if (useRealApi && normalizedBaseUrl.isNotBlank()) {
            RealRemodelApi(
                baseUrl = normalizedBaseUrl,
                openImageStream = openImageStream
            )
        } else {
            MockRemodelApi()
        }
    }
}
