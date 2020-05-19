package eu.tib.orkg.prototype.statements.domain.model.jpa

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
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
    }
