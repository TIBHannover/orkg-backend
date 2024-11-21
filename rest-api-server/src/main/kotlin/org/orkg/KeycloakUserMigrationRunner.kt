package org.orkg

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.time.ZoneOffset
import org.keycloak.representations.idm.UserRepresentation
import org.orkg.auth.domain.Role
import org.orkg.auth.domain.User
import org.orkg.auth.output.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

private const val usersPerFile = 100

@Component
@Profile("userMigrations")
class KeycloakUserMigrationRunner(
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Value("\${orkg.export.users.file-dir:#{null}}")
    private val fileDir: String? = null

    @Value("\${orkg.export.users.target-realm:#{null}}")
    private val realm: String? = null

    override fun run(args: ApplicationArguments?) {
        logger.info("Starting user export...")
        val users = mutableListOf<UserRepresentation>()
        userRepository.forEachPage { page ->
            users += page.map { user ->
                UserRepresentation().apply {
                    id = user.id.toString()
                    username = "user_${user.id.toString().replace("-", "")}"
                    email = user.email
                    isEmailVerified = true
                    createdTimestamp = user.createdAt.toInstant(ZoneOffset.UTC).toEpochMilli()
                    isEnabled = true
                    requiredActions = listOf(
                        "TERMS_AND_CONDITIONS",
                        "UPDATE_PASSWORD"
                    )
                    realmRoles = listOf(
                        "default-roles-$realm"
                    )
                    groups = when {
                        Role.ADMIN in user.roles -> listOf("admins")
                        Role.CURATOR in user.roles -> listOf("curators")
                        else -> emptyList()
                    }
                    singleAttribute<UserRepresentation>("displayName", user.displayName)
                }
            }
        }
        val realmUsers = RealmUsers(users)
        val file = File(fileDir, "$realm-users-0.json")
        file.writeText(objectMapper.writeValueAsString(realmUsers))
        logger.info("Finished user export.")
    }

    private fun UserRepository.forEachPage(action: (List<User>) -> Unit) {
        var page = findAll(pageable = PageRequest.of(0, usersPerFile))
        action(page.content)
        while (page.hasNext()) {
            page = findAll(pageable = PageRequest.of(page.number + 1, usersPerFile))
            action(page.content)
        }
    }

    data class RealmUsers(
        val users: List<UserRepresentation>
    )
}
