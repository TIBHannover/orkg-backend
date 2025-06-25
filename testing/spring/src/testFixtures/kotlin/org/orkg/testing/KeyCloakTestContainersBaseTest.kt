package org.orkg.testing

import dasniko.testcontainers.keycloak.KeycloakContainer
import org.junit.jupiter.api.BeforeAll
import org.orkg.constants.BuildConfig
import org.testcontainers.junit.jupiter.Testcontainers

const val KEYCLOAK_REALM = "orkg"
const val KEYCLOAK_CLIENT_ID = "orkg-frontend"
const val KEYCLOAK_CLIENT_SECRET = "**********"

@Testcontainers
abstract class KeycloakTestContainersBaseTest {
    companion object {
        // It is important to not use @Containers here, so we can manage the life-cycle.
        // We instantiate only one container per test class.
        @JvmStatic
        protected val container: KeycloakContainer = KeycloakContainer(BuildConfig.CONTAINER_IMAGE_KEYCLOAK)
            .withRealmImportFile("import/realm_orkg.json")
            .withExposedPorts(8080, 8443, 8787, 9000)

        // Start the container once per class. This needs to be done via a static method.
        // If @TestInstance(PER_CLASS) is used, Spring fails to set up the application context.
        // Ryuk will manage the shut-down, so shutdown method is required.
        @JvmStatic
        @BeforeAll
        fun startContainer() = container.start()
    }
}
