package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.neo4j.ogm.annotation.Property
import java.time.OffsetDateTime

open class AuditableEntity(
    @Property("created_at")
    var createdAt: OffsetDateTime? = OffsetDateTime.now()
)
