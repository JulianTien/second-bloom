package com.scf.secondbloom.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class SecondBloomAuthModelsTest {

    @Test
    fun buildAuthDisplayName_prefersFullName_thenParts_thenFallback() {
        assertEquals(
            "Ada Lovelace",
            buildAuthDisplayName(
                fullName = " Ada Lovelace ",
                firstName = "Ada",
                lastName = "Lovelace"
            )
        )

        assertEquals(
            "Ada Lovelace",
            buildAuthDisplayName(
                fullName = "   ",
                firstName = "Ada",
                lastName = "Lovelace"
            )
        )

        assertEquals(
            "Clerk user",
            buildAuthDisplayName(
                fullName = null,
                firstName = " ",
                lastName = null
            )
        )
    }
}
