package eu.tib.orkg.prototype.auth.persistence

import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull


@Entity
@Table(name="organization_user_mapper")
class OrganizationUserMapperEntity {
    @Id
    var id: UUID? = null

    @Column(name= "user_id")
    var userId: UUID? = null

    @Column(name= "organization_id")
    var organizationId: UUID? = null

    @NotNull
    var created: LocalDateTime = LocalDateTime.now()
}
