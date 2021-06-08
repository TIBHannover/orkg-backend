package eu.tib.orkg.prototype.auth.persistence

import com.sun.istack.NotNull
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

//Dont use this - there is already a table - organizations
@Entity
@Table(name="organization")
class Organization {
    @Id
    var id: UUID? = null

    @Column(name="organization_name")
    var organizationName: String? = null

    @NotNull
    var created: LocalDateTime = LocalDateTime.now()
}
