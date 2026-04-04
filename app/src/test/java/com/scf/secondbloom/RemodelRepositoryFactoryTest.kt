package com.scf.secondbloom.data.repository

import com.scf.secondbloom.data.remote.RealRemodelApi
import com.scf.secondbloom.data.remote.mock.MockRemodelApi
import org.junit.Assert.assertTrue
import org.junit.Test

class RemodelRepositoryFactoryTest {

    @Test
    fun createApi_returnsMockApi_whenBaseUrlIsBlank() {
        val api = RemodelRepositoryFactory.createApi(
            useRealApi = true,
            baseUrl = "   ",
            openImageStream = { null }
        )

        assertTrue(api is MockRemodelApi)
    }

    @Test
    fun createApi_returnsRealApi_whenFlagAndBaseUrlArePresent() {
        val api = RemodelRepositoryFactory.createApi(
            useRealApi = true,
            baseUrl = "https://secondbloom.test",
            openImageStream = { null }
        )

        assertTrue(api is RealRemodelApi)
    }
}
