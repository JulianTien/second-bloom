package com.scf.secondbloom.data.local

import android.content.Context

object RemodelHistoryRepositoryFactory {
    fun create(context: Context): RemodelHistoryRepository =
        DefaultRemodelHistoryRepository(
            localDataSource = FileRemodelHistoryLocalDataSource(
                context = context.applicationContext
            )
        )
}
