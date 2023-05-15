package eu.tib.orkg.prototype

import com.fasterxml.jackson.databind.ObjectMapper
import eu.tib.orkg.prototype.auth.adapter.output.jpa.spring.internal.JpaUserRepository
import eu.tib.orkg.prototype.auth.spi.UserRepository
import eu.tib.orkg.prototype.statements.spi.EntityRepository
import java.time.ZoneOffset
import java.util.*
import org.keycloak.OAuth2Constants.PASSWORD
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component


@Component
@Profile("keycloak")
class KeycloakMigrationRunner(
    @Value("\${keycloak.credentials.secret}")
    private val secretKey: String,
    @Value("\${keycloak.resource}")
    private val clientId: String,
    @Value("\${keycloak.auth-server-url}")
    private val authUrl: String,
    @Value("\${keycloak.realm}")
    private val realm: String,
    private val userRepository: JpaUserRepository,
    private val objectMapper: ObjectMapper,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Autowired
    private lateinit var context: ConfigurableApplicationContext

    override fun run(args: ApplicationArguments?) {
        val keycloak = KeycloakBuilder.builder()
            .grantType(PASSWORD)
            .serverUrl(authUrl)
            .realm(realm)
            .clientId("admin-cli")
            .username("admin")
            .password("admin")
            .build()

        userRepository.findAll().forEach {
            val response = keycloak
                .realm(realm)
                .users()
                .create(
                    UserRepresentation().apply {
                        username = it.email
                        email = it.email
                        createdTimestamp = it.created.toEpochSecond(ZoneOffset.UTC)
                        isEnabled = true
                        credentials = listOf(
                            CredentialRepresentation().apply {
                                type = CredentialRepresentation.PASSWORD
                                userLabel = "password"
                                createdDate = it.created.toEpochSecond(ZoneOffset.UTC)
                                isTemporary = false
                                secretData = objectMapper.writeValueAsString(
                                    mapOf(
                                        "value" to it.password!!.substring(8),
                                        "salt" to "",
                                        "additionalParameters" to emptyMap<String, String>()
                                    )
                                )
                                credentialData = objectMapper.writeValueAsString(
                                    mapOf(
                                        "hashIterations" to 10,
                                        "algorithm" to "bcrypt",
                                        "additionalParameters" to emptyMap<String, String>()
                                    )
                                )
                            }
                        )
                        requiredActions = listOf("UPDATE_PROFILE")
                    }
                )
            if (response.status != HttpStatus.CREATED.value()) {
                logger.error("Could not migrate user profile ${it.id}")
                return@forEach
            }
            val newId = response.getHeaderString("Location").split("users/")[1]
        }
        context.close()
    }
}
