package eu.tib.orkg.prototype.statements.domain.model.neo4j

import java.time.OffsetDateTime
import org.neo4j.ogm.annotation.Property

open class AuditableEntity(
    @Property("created_at")
    var createdAt: OffsetDateTime? = OffsetDateTime.now()
)
