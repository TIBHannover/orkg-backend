package eu.tib.orkg.prototype.statements.domain.model.jpa

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table
import com.fasterxml.jackson.annotation.JsonIgnore
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

        @JsonIgnore
        @ManyToMany
        @JoinTable(
            name = "observatory_organizations",
            joinColumns = [JoinColumn(name = "organization_id", referencedColumnName = "id")],
            inverseJoinColumns = [JoinColumn(name = "observatory_id", referencedColumnName = "id")])
        var observatories: Set<ObservatoryEntity>? = null
    }
