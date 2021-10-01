package eu.tib.orkg.prototype.auth.persistence

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
import java.security.Principal
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import java.util.logging.Logger

class KeycloakUserEntity(
    private val orgUserMapperRepository: OrgUserMapperRepository,
    private val observatoryUserMapperRepository: ObservatoryUserMapperRepository,
    private val keycloakRestTemplate: KeycloakRestTemplate
) {
    val id: UUID? = null
    val email: String? = null
    val displayName: String? = null
    val enabled: Boolean? = null
    val created: LocalDateTime? = null

    fun getAccessToken(principal: Principal): AccessToken {
        val token: KeycloakAuthenticationToken = principal as KeycloakAuthenticationToken
        val keycloakPrincipal = token.principal as KeycloakPrincipal<*>
        val session = keycloakPrincipal.keycloakSecurityContext
        return session.token
    }


    fun toContributor() = Contributor(
        id = ContributorId(this.id!!),
        name = this.displayName!!,
        joinedAt = OffsetDateTime.of(this.created, ZoneOffset.UTC),
        organizationId = mutableListOf(OrganizationId.createUnknownOrganization())[0], //should change to list
        observatoryId = mutableListOf(ObservatoryId.createUnknownObservatory())[0],
        email = this.email!!
    )

    private fun getOrganizationIdList(userId: String): List<OrganizationId>{
        val orgUserEntities = orgUserMapperRepository.findAllByUserId(UUID.fromString(userId))
        val orgList = mutableListOf<OrganizationId>()

        if(orgUserEntities.isNotEmpty()){
            orgUserEntities.map {
                orgList.add(OrganizationId(it.organizationId!!))
            }
            return orgList
        }

        return mutableListOf(OrganizationId.createUnknownOrganization())
    }

    private fun getObservatoryIdList(userId: String): List<ObservatoryId>{
        val obsUserEntities = observatoryUserMapperRepository.findAllByUserId(UUID.fromString(userId))
        val obsList = mutableListOf<ObservatoryId>()

        if(obsUserEntities.isNotEmpty()){
            obsUserEntities.map {
                obsList.add(ObservatoryId(it.observatoryId!!))
            }
            return obsList
        }

        return mutableListOf(ObservatoryId.createUnknownObservatory())

    }
}
