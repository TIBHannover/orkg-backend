package org.orkg.testing.dsl.api

import org.orkg.testing.drivers.SystemDriver
import org.orkg.testing.dsl.junit.TestContext
import java.util.UUID

class RegistrationAPI(
    private val systemDriver: SystemDriver,
    private val testContext: TestContext,
) {
    fun createUser(name: String, password: String = "Pa$\$w0rd", displayName: String? = null) {
        // Defaults values to into the message signature.
        // In the original DSL, the parameter definition is constructed inside this function.
        val uniqueUser = testContext.lookupOrCreateUsername(name)

        // In the original DSL, all parameters would be passed in a single argument
        registerUser(uniqueUser, password, displayName)

        // Other default actions could go here, like logging in depending on a flag
    }

    private fun registerUser(name: String, password: String, displayName: String?): UUID {
        // In the original DSL, all parameters would be taken from a single argument
        val registrationUser = RegistrationData(
            username = name,
            // "displayName" has a character limit which needs be respected (currently 35 chars).
            // Other restrictions may apply as well (e.g., invalid chars).
            displayName = displayName ?: name,
            email = "$name@example.org",
            password = password,
        )

        val accountId = systemDriver.registrationDriver.registerUser(registrationUser)
        testContext.storeUserDetails(accountId, registrationUser)
        return accountId
    }
}

data class RegistrationData(
    val username: String,
    val displayName: String,
    val email: String,
    val password: String,
)
