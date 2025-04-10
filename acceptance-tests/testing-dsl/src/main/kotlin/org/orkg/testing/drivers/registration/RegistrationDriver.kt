package org.orkg.testing.drivers.registration

import org.orkg.testing.drivers.Driver
import org.orkg.testing.drivers.registration.email.MailBox
import org.orkg.testing.drivers.registration.keycloak.Keycloak
import org.orkg.testing.drivers.registration.keycloak.KeycloakRegistrationFlow
import org.orkg.testing.dsl.api.PublicAPI.Tokens
import org.orkg.testing.dsl.api.RegistrationData
import org.orkg.world.Environment
import java.util.UUID

/**
 * Keycloak-specific registration driver.
 */
class RegistrationDriver(private val environment: Environment) : Driver {
    private val keycloak: Keycloak = Keycloak.from(environment)

    fun registerUser(user: RegistrationData): UUID {
        val realm = environment["ORKG_KEYCLOAK_REALM"] ?: error("Expected environment variable ORKG_KEYCLOAK_REALM to be set!")
        KeycloakRegistrationFlow(
            keycloak = keycloak,
            mailBox = MailBox.from(environment, user.email)
        ).register(user)
        // Get ID from Keycloak for the new user. This is internal and not stable, we need to fetch it directly.
        val keycloakUser = keycloak.adminClient.realm(realm).users().search(user.username)[0]
        assert(keycloakUser.username == user.username) { "Got wrong user data from Keycloak! Expected <${user.username}> but found <${keycloakUser.username}>." }
        return UUID.fromString(keycloakUser.id)
    }

    fun login(user: RegistrationData): Tokens = keycloak.login(username = user.username, password = user.password)
        .let { Tokens(it.accessToken, it.refreshToken) }
}
