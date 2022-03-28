package eu.tib.orkg.prototype.auth.keycloak

import eu.tib.orkg.prototype.auth.service.ObservatoryUserMapperRepository
import eu.tib.orkg.prototype.auth.service.OrgUserMapperRepository
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import org.keycloak.KeycloakPrincipal
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.keycloak.representations.AccessToken
import org.springframework.stereotype.Component
import java.net.URI
import java.security.Principal
import java.time.OffsetDateTime
import java.util.UUID

@Component
class KeycloakServiceHandler(
    private val orgUserMapperRepository: OrgUserMapperRepository,
    private val keycloakRestTemplate: KeycloakRestTemplate,
    private val observatoryUserMapperRepository: ObservatoryUserMapperRepository
) {

    fun getAccessToken(principal: Principal): AccessToken {
        val token: KeycloakAuthenticationToken = principal as KeycloakAuthenticationToken
        val keycloakPrincipal = token.principal as KeycloakPrincipal<*>
        val session = keycloakPrincipal.keycloakSecurityContext
        return session.token
    }

    fun getUserDetails(principal: Principal): KeycloakUserDetails? {
        val accessToken = getAccessToken(principal)
        if (accessToken.id == null) {
            return null
        }

        return KeycloakUserDetails(UUID.fromString(accessToken.id),
            mutableSetOf<String>("ROLE_USER"),
            true,
            accessToken.name)
    }

    fun getUserPrincipal(principal: Principal): KeycloakUserPrincipal {
        val accessToken = getAccessToken(principal)
        return KeycloakUserPrincipal(
            username = UUID.fromString(accessToken.id!!),
            roles = mutableSetOf(),
            enabled = true,
            displayName = accessToken.name
        )
    }

    fun getContributor(principal: Principal): Contributor {
        val accessToken = getAccessToken(principal)
        return Contributor(
            id = ContributorId(accessToken.id),
            name = accessToken.name,
            joinedAt = OffsetDateTime.now(), // should change -> take from Keycloak
            organizationId = getOrganizationIdList(accessToken.id)[0], // should change to list
            observatoryId = getObservatoryIdList(accessToken.id)[0],
            email = accessToken.email
        )
    }

    private fun getOrganizationIdList(userId: String): List<OrganizationId> {
        val orgUserEntities = orgUserMapperRepository.findAllByUserId(UUID.fromString(userId))
        val orgList = mutableListOf<OrganizationId>()

        if (orgUserEntities.isNotEmpty()) {
            orgUserEntities.map {
                orgList.add(OrganizationId(it.organizationId!!))
            }
            return orgList
        }

        return mutableListOf(OrganizationId.createUnknownOrganization())
    }

    private fun getObservatoryIdList(userId: String): List<ObservatoryId> {
        val obsUserEntities = observatoryUserMapperRepository.findAllByUserId(UUID.fromString(userId))
        val obsList = mutableListOf<ObservatoryId>()

        if (obsUserEntities.isNotEmpty()) {
            obsUserEntities.map {
                obsList.add(ObservatoryId(it.observatoryId!!))
            }
            return obsList
        }

        return mutableListOf(ObservatoryId.createUnknownObservatory())
    }

    fun getUsers(): List<*>? {
        val response = keycloakRestTemplate.getForEntity(URI.create("http://localhost:8180/auth/admin/realms/myrealm/users"),
            List::class.java
        ).body
        return response
    }
}

data class KeycloakUserDetails(
    val username: UUID,
    val roles: Set<String>,
    val enabled: Boolean,
    val displayName: String
)

data class KeycloakUserPrincipal(
    val username: UUID,
    val roles: Set<String>,
    val enabled: Boolean,
    val displayName: String
)
