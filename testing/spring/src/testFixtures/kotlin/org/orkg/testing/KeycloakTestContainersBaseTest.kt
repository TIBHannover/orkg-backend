package eu.tib.orkg.prototype.testing

import dasniko.testcontainers.keycloak.KeycloakContainer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers

const val KEYCLOAK_VERSION = "22.0.3"
const val KEYCLOAK_TEST_REALM = "orkg-dev"

@Testcontainers
abstract class KeycloakTestContainersBaseTest {
    companion object {
        // It is important to not use @Containers here, so we can manage the life-cycle.
        // We instantiate only one container per test class.
        @JvmStatic
        protected val container: KeycloakContainer = KeycloakContainer("quay.io/keycloak/keycloak:$KEYCLOAK_VERSION")
            .withRealmImportFile("keycloak/import/realm_orkg-dev_full.json")
            .withExposedPorts(8080)

        // Start the container once per class. This needs to be done via a static method.
        // If @TestInstance(PER_CLASS) is used, Spring fails to set up the application context.
        // Ryuk will manage the shut-down, so shutdown method is required.
        @JvmStatic
        @BeforeAll
        fun startContainer() = container.start()
    }
}
