package org.orkg.community.adapter.input.keycloak

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.ws.rs.NotFoundException
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.representations.idm.AdminEventRepresentation
import org.keycloak.representations.idm.EventRepresentation
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.EventType
import org.orkg.community.domain.internal.MD5Hash
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.KeycloakEventStateRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpClient.Version.HTTP_1_1
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Optional

@Service
@Profile("development", "docker", "production")
class KeycloakEventProcessor(
    private val contributorRepository: ContributorRepository,
    private val keycloak: Keycloak,
    private val keycloakEventStateRepository: KeycloakEventStateRepository,
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper,
    @Value("\${orkg.keycloak.host}")
    private val host: String,
    @Value("\${orkg.keycloak.realm}")
    private val realm: String,
    @Value("\${orkg.keycloak.event-poll-chunk-size}")
    private val eventPollChunkSize: Int,
) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Scheduled(cron = "\${orkg.keycloak.event-poll-schedule}")
    fun processEvents() {
        var offset = keycloakEventStateRepository.findById(EventType.USER_EVENT)
        val events = getEventsReversed(
            types = listOf("REGISTER", "UPDATE_PROFILE", "DELETE_ACCOUNT"),
            firstResult = offset,
            maxResults = eventPollChunkSize
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
        val events = getAdminEventsReversed(
            operationTypes = listOf("CREATE", "UPDATE", "DELETE"),
            firstResult = offset,
            maxResults = eventPollChunkSize
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

    private fun getAdminEventsReversed(
        operationTypes: List<String>? = null,
        authRealm: String? = null,
        authClient: String? = null,
        authUser: String? = null,
        authIpAddress: String? = null,
        resourcePath: String? = null,
        resourceTypes: List<String>? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        firstResult: Int? = null,
        maxResults: Int? = null,
    ): List<AdminEventRepresentation> {
        val uri = UriComponentsBuilder.fromUriString(host)
            .path("/admin/realms/$realm/events-reversed/admin-events-reversed")
            .queryParamIfPresent("operationTypes", Optional.ofNullable(operationTypes))
            .queryParamIfPresent("authRealm", Optional.ofNullable(authRealm))
            .queryParamIfPresent("authClient", Optional.ofNullable(authClient))
            .queryParamIfPresent("authUser", Optional.ofNullable(authUser))
            .queryParamIfPresent("authIpAddress", Optional.ofNullable(authIpAddress))
            .queryParamIfPresent("resourcePath", Optional.ofNullable(resourcePath))
            .queryParamIfPresent("resourceTypes", Optional.ofNullable(resourceTypes))
            .queryParamIfPresent("dateFrom", Optional.ofNullable(dateFrom))
            .queryParamIfPresent("dateTo", Optional.ofNullable(dateTo))
            .queryParam("first", firstResult ?: 0)
            .queryParamIfPresent("max", Optional.ofNullable(maxResults))
            .build()
            .toUri()
        val response = sendRequest(uri)
        if (response.statusCode() != 200) {
            logger.warn("Could not fetch admin events. Status: ${response.statusCode()}")
            return emptyList()
        }
        return response.body().let { objectMapper.readValue(it, object : TypeReference<List<AdminEventRepresentation>>() {}) }
    }

    private fun getEventsReversed(
        types: List<String>? = null,
        client: String? = null,
        user: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        ipAddress: String? = null,
        firstResult: Int? = null,
        maxResults: Int? = null,
    ): List<EventRepresentation> {
        val uri = UriComponentsBuilder.fromUriString(host)
            .path("/admin/realms/$realm/events-reversed/events-reversed")
            .queryParamIfPresent("type", Optional.ofNullable(types))
            .queryParamIfPresent("client", Optional.ofNullable(client))
            .queryParamIfPresent("user", Optional.ofNullable(user))
            .queryParamIfPresent("dateFrom", Optional.ofNullable(dateFrom))
            .queryParamIfPresent("dateTo", Optional.ofNullable(dateTo))
            .queryParamIfPresent("ipAddress", Optional.ofNullable(ipAddress))
            .queryParam("first", firstResult ?: 0)
            .queryParamIfPresent("max", Optional.ofNullable(maxResults))
            .build()
            .toUri()
        val response = sendRequest(uri)
        if (response.statusCode() != 200) {
            logger.warn("Could not fetch user events. Status: ${response.statusCode()}")
            return emptyList()
        }
        return response.body().let { objectMapper.readValue(it, object : TypeReference<List<EventRepresentation>>() {}) }
    }

    private fun sendRequest(uri: URI): HttpResponse<String> {
        val request = HttpRequest.newBuilder(uri)
            .version(HTTP_1_1) // JDK 21 does not handle HTTP/2 GOAWAY frames correctly. See https://bugs.openjdk.org/browse/JDK-8335181
            .header("Accept", MediaType.APPLICATION_JSON_VALUE)
            .header("Authorization", "Bearer ${keycloak.tokenManager().accessTokenString}")
            .GET()
            .build()
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
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
            emailMD5 = MD5Hash.fromEmail(user.email),
            isCurator = roles.any { it.name == "curator" },
            isAdmin = roles.any { it.name == "admin" }
        )
    }
}
