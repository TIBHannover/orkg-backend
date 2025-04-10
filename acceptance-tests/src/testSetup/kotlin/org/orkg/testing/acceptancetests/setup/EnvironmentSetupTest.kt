package org.orkg.testing.acceptancetests.setup

import org.junit.jupiter.api.Tag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.testing.dsl.junit.DslTestCase

@Tag("setup")
class EnvironmentSetupTest : DslTestCase() {
    @ParameterizedTest(name = "environment variable \"{0}\" is not null")
    @ValueSource(
        strings = [
            "API_HOST",
            "API_TCP_8080",
            "KEYCLOAK_HOST",
            "KEYCLOAK_TCP_8080",
            "MAILSERVER_HOST",
            "MAILSERVER_TCP_8025",
            "ORKG_KEYCLOAK_REALM",
            "ORKG_KEYCLOAK_USER_CLIENT_ID",
            "ORKG_KEYCLOAK_USER_CLIENT_SECRET",
            "ORKG_KEYCLOAK_ADMIN_USERNAME",
            "ORKG_KEYCLOAK_ADMIN_PASSWORD",
            "ORKG_KEYCLOAK_ADMIN_REALM",
            "ORKG_KEYCLOAK_ADMIN_CLIENT_ID",
        ]
    )
    fun `ensure all environment variables are set correctly`(variable: String) {
        val value = System.getenv(variable)
        logger.info("Checking presence of environment variable: $variable=$value")
        assert(value != null)
    }
}
