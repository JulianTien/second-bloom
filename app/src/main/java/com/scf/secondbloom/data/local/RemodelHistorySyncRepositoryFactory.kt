package com.scf.secondbloom.data.local

import android.content.Context
import com.scf.secondbloom.data.historysync.HistoryAuthTokenProvider
import com.scf.secondbloom.data.historysync.HistorySyncRepository
import com.scf.secondbloom.data.historysync.HttpHistorySyncApi

object RemodelHistorySyncRepositoryFactory {
    fun create(
        context: Context,
        baseUrl: String,
        accessTokenProvider: HistoryAuthTokenProvider,
        namespace: String = "default"
    ): HistorySyncRepository {
        val applicationContext = context.applicationContext
        val localDataSource = FileRemodelHistoryLocalDataSource(applicationContext)

        return HistorySyncRepository(
            api = HttpHistorySyncApi(baseUrl = baseUrl),
            snapshotStore = FileRemodelHistorySnapshotStore(localDataSource),
            stateStore = SharedPrefsHistorySyncStateStore(
                context = applicationContext,
                namespace = namespace
            ),
            accessTokenProvider = accessTokenProvider
        )
    }
}
