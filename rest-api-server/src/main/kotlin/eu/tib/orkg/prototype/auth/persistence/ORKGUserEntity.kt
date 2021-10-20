package eu.tib.orkg.prototype.auth.persistence

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull


@Entity
@Table(name="orkg_user")
class ORKGUserEntity {
    @Id
    var id: UUID? = null

    @Column(name="old_id")
    var oldID: UUID? = null

    @Column(name="keycloak_id")
    var keycloakID: UUID? = null

    @Column(name="display_name")
    var displayName: String? = null

    @Column(name="email")
    var email: String? = null

    @Column(name="first_name")
    var firstName: String? = null

    @Column(name="last_name")
    var lastName: String? = null

    @Column(name="name")
    var name: String? = null

    @Column(name="organization_id")
    var organizationId: UUID? = null

    @Column(name="observatory_id")
    var observatoryId: UUID? = null

    @NotNull
    var created: LocalDateTime = LocalDateTime.now()

    fun toUserPrincipal()  =
        KeycloakUserPrincipal(
            username = oldID,
            roles = mutableSetOf(),
            enabled = true,
            displayName = ""
        )


    fun toContributor(): Contributor{
        val newContributorId = if (this.oldID != null){
            ContributorId(this.oldID!!)
        }else{
            ContributorId(this.keycloakID!!)
        }
        return Contributor(
            id = newContributorId,
            name = "Testing contributor",
            joinedAt = OffsetDateTime.of(this.created, ZoneOffset.UTC),
            organizationId = this.organizationId?.let { OrganizationId(it) } ?: OrganizationId.createUnknownOrganization(),
            observatoryId = this.observatoryId?.let { ObservatoryId(it) } ?: ObservatoryId.createUnknownObservatory(),
            email = "testcontributor@test.com")
    }

    /*fun toContributor() =Contributor(
            id = ContributorId(this.oldID!!),
            name = "Testing contributor",
            joinedAt = OffsetDateTime.of(this.created, ZoneOffset.UTC),
            organizationId = this.organizationId?.let { OrganizationId(it) } ?: OrganizationId.createUnknownOrganization(),
            observatoryId = this.observatoryId?.let { ObservatoryId(it) } ?: ObservatoryId.createUnknownObservatory(),
            email = "testcontributor@test.com")*/
}


data class KeycloakUserPrincipal(
    val username: UUID? = null,
    val roles: MutableSet<String> = mutableSetOf(),
    val enabled: Boolean? = true,
    val displayName: String? = null
)
