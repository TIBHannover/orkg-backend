package eu.tib.orkg.prototype.statements.domain.model.jpa

import eu.tib.orkg.prototype.statements.domain.model.Organization
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.Table
import javax.validation.constraints.NotBlank

@Entity
@Table(name = "organizations")
class OrganizationEntity {
        @Id
        var id: UUID? = null

        @NotBlank
        var name: String? = null

        @Column(name = "created_by")
        var createdBy: UUID? = null

        @ManyToMany(mappedBy = "organizations", fetch = FetchType.LAZY)
        var observatories: Set<ObservatoryEntity>? = emptySet()

        fun toOrganization() = Organization(id, name, createdBy, observatories)
    }
