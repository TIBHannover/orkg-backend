package org.orkg.testing.dsl.junit

import org.junit.jupiter.api.TestInfo
import org.orkg.testing.dsl.api.PublicAPI.Tokens
import org.orkg.testing.dsl.api.RegistrationData
import org.orkg.world.ControlledRandomness
import org.orkg.world.World
import java.math.BigInteger
import java.security.MessageDigest
import java.util.UUID
import kotlin.jvm.optionals.getOrElse
import kotlin.random.nextUInt

class TestContext(
    private val info: TestInfo,
    world: World,
) {
    /**
     * A unique identifier for the test that can be used within the context to make data unique and traceable to the test.
     *
     * The value is calculated from the test method signature, so it is stable between runs, unless a test signature is modified,
     * e.g., by moving or renaming the test case, or injecting new parameters.
     */
    @Suppress("unused")
    val testId: String by lazy {
        val uniqueTestName: String = info.testClass
            .getOrElse { error("No test class information available! Unable to generate unique test class identifier!") }
            .let { "${it.name}#${info.displayName}" }
        val hash = MessageDigest.getInstance("SHA-1").digest(uniqueTestName.toByteArray())
        BigInteger(1, hash).toString(16).substring(0, Long.SIZE_BYTES)
    }

    // This is part is still a bit hacky, and I am not sure if it can be simplified:
    // We need unique values per test, e.g., for generating extensions to usernames, but need it to be deterministic.
    // (If the last part of the preposition does not hold, we could just use the system RNG, I think.)
    // The problem appears when we take the same action in several test methods.
    // Because the RNG is set on the class level, it is the same, producing the same extensions, leading to test failures.
    // To make the values unique per test, we create a new seed by mixing the test ID and a "random" value from the RNG passed to us.
    // A new RNG is created with this seed here, so it is unique to each test context.
    // This ensures the values are deterministic across runs, but still produce different results in each test method.
    val seed = testId.toLong(16) xor world.randomness.rng.nextLong()
    private val random = ControlledRandomness(seed).rng

    // Storage containers for various info that other tests might need or use.
    private val usernames: MutableMap<String, String> = mutableMapOf()
    private val registrationInfo: MutableMap<UUID, RegistrationData> = mutableMapOf()
    private val tokens: MutableMap<String, Tokens> = mutableMapOf()

    fun lookupOrCreateUsername(username: String): String =
        usernames.getOrPut(username) { "$username-${random.nextUInt()}" }

    fun storeUserDetails(accountId: UUID, registrationUser: RegistrationData) {
        registrationInfo[accountId] = registrationUser
    }

    fun loadUserDetails(username: String): RegistrationData {
        val effectiveUsername =
            usernames[username] ?: error("User <$username> was not found in the list of users. Was it registered?")
        val data = registrationInfo.values.find { it.username == effectiveUsername }
            ?: error("No registration data for user <$effectiveUsername>. Did the registration succeed?")
        return data
    }

    fun storeTokens(username: String, userToken: Tokens) {
        tokens[username] = userToken
    }
}
