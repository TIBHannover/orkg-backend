package org.orkg.testing.dsl.api

import org.orkg.testing.drivers.SystemDriver
import org.orkg.testing.dsl.junit.TestContext

class PublicAPI(
    private val systemDriver: SystemDriver,
    private val testContext: TestContext,
) {
    fun login(user: String) {
        val details = testContext.loadUserDetails(user)
        // We reuse the registration driver here, because it is related.
        val tokens = systemDriver.registrationDriver.login(details)
        testContext.storeTokens(details.username, tokens)
    }

    data class Tokens(
        val accessToken: String,
        val refreshToken: String,
    )
}
