package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.neo4j.ogm.annotation.Property
import org.springframework.data.annotation.CreatedDate
import java.time.OffsetDateTime

open class AuditableEntity(
    @Property("created")
    @CreatedDate
    var createdAt: OffsetDateTime? = null
)
