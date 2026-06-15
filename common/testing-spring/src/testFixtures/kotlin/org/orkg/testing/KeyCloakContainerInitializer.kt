package org.orkg.testing

import dasniko.testcontainers.keycloak.KeycloakContainer
import org.orkg.constants.BuildConfig
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.images.PullPolicy

const val KEYCLOAK_REALM = "orkg"
const val KEYCLOAK_CLIENT_ID = "orkg-frontend"
const val KEYCLOAK_CLIENT_SECRET = "**********"

class KeycloakTestContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    companion object {
        val keycloakContainer: KeycloakContainer = KeycloakContainer(BuildConfig.CONTAINER_IMAGE_KEYCLOAK)
            .withRealmImportFile("import/realm_orkg.json")
            .withExposedPorts(8080, 8443, 8787, 9000)
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        keycloakContainer.start()
    }
}
