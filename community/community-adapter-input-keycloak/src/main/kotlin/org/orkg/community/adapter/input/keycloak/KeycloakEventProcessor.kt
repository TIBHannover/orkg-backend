package org.orkg.community.adapter.input.keycloak

import jakarta.ws.rs.NotFoundException
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.representations.idm.AdminEventRepresentation
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.EventType
import org.orkg.community.domain.internal.SHA256
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.KeycloakEventStateRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@Service
@Profile("development", "docker", "production")
class KeycloakEventProcessor(
    private val contributorRepository: ContributorRepository,
    private val keycloak: Keycloak,
    private val keycloakEventStateRepository: KeycloakEventStateRepository,
    @param:Value("\${orkg.keycloak.realm}")
    private val realm: String,
    @param:Value("\${orkg.keycloak.event-poll-chunk-size}")
    private val eventPollChunkSize: Int,
) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Scheduled(cron = "\${orkg.keycloak.event-poll-schedule}")
    fun processEvents() {
        var offset = keycloakEventStateRepository.findById(EventType.USER_EVENT)
        val events = keycloak.realm(realm).getEvents(
            // @formatter:off
            /* types = */ listOf("REGISTER", "UPDATE_PROFILE", "DELETE_ACCOUNT"),
            /* client = */ null,
            /* user = */ null,
            /* dateFrom = */ null,
            /* dateTo = */ null,
            /* ipAddress = */ null,
            /* firstResult = */ offset,
            /* maxResults = */ eventPollChunkSize,
            /* direction = */ "asc",
            // @formatter:on
        )
        events.forEach { event ->
            when (event.type) {
                "REGISTER", "UPDATE_PROFILE" -> fetchUserAsConributorById(event.userId)?.also(contributorRepository::save)
                "DELETE_ACCOUNT" -> contributorRepository.deleteById(ContributorId(event.userId))
            }
            keycloakEventStateRepository.save(EventType.USER_EVENT, ++offset)
        }
    }

    @Scheduled(cron = "\${orkg.keycloak.event-poll-schedule}")
    fun processAdminEvents() {
        var offset = keycloakEventStateRepository.findById(EventType.ADMIN_EVENT)
        val events = keycloak.realm(realm).getAdminEvents(
            // @formatter:off
            /* operationTypes = */ listOf("CREATE", "UPDATE", "DELETE"),
            /* authRealm = */ null,
            /* authClient = */ null,
            /* authUser = */ null,
            /* authIpAddress = */ null,
            /* resourcePath = */ null,
            /* resourceTypes = */ null,
            /* dateFrom = */ null,
            /* dateTo = */ null,
            /* firstResult = */ offset,
            /* maxResults = */ eventPollChunkSize,
            /* direction = */ "asc",
            // @formatter:on
        )
        events.forEach { event ->
            if (event.isUserEvent) {
                val userId = extractUserIdFromResourcePath(event.resourcePath)
                when (event.operationType) {
                    "CREATE", "UPDATE" -> fetchUserAsConributorById(userId)?.also(contributorRepository::save)
                    "DELETE" -> contributorRepository.deleteById(ContributorId(userId))
                }
            } else if (event.isRoleEvent) {
                val userId = extractUserIdFromResourcePath(event.resourcePath)
                fetchUserAsConributorById(userId)?.also(contributorRepository::save)
            }
            keycloakEventStateRepository.save(EventType.ADMIN_EVENT, ++offset)
        }
    }

    private fun fetchUserById(id: String): UserResource =
        keycloak.realm(realm).users().get(id)

    private fun fetchUserAsConributorById(id: String) =
        fetchUserById(id).toContributor()

    private fun extractUserIdFromResourcePath(resourcePath: String): String = resourcePath.substringAfter("/").substringBefore("/")

    private val AdminEventRepresentation.isUserEvent get() = resourceType == "USER"

    private val AdminEventRepresentation.isRoleEvent get() =
        (resourceType == "REALM_ROLE_MAPPING" || resourceType == "CLIENT_ROLE_MAPPING" || resourceType == "GROUP_MEMBERSHIP") &&
            (operationType == "CREATE" || operationType == "DELETE")

    private fun UserResource.toContributor(): Contributor? {
        val user = try {
            toRepresentation()
        } catch (e: NotFoundException) {
            return null
        }
        val displayName = user.firstAttribute("displayName")
        if (user.email == null || displayName == null) {
            logger.warn("Skipped processing of user ${user.id} because email or display name is null.")
            return null
        }
        val roles = roles().realmLevel().listEffective()
        return Contributor(
            id = ContributorId(user.id),
            name = displayName,
            joinedAt = OffsetDateTime.ofInstant(Instant.ofEpochMilli(user.createdTimestamp), ZoneId.systemDefault()),
            organizationId = OrganizationId.UNKNOWN,
            observatoryId = ObservatoryId.UNKNOWN,
            emailHash = SHA256.fromEmail(user.email),
            isCurator = roles.any { it.name == "curator" },
            isAdmin = roles.any { it.name == "admin" }
        )
    }
}
