package eu.tib.orkg.prototype.statements.domain.model.neo4j

import org.springframework.data.neo4j.core.schema.Property
import java.time.OffsetDateTime

open class AuditableEntity(
    @Property("created_at")
    var createdAt: OffsetDateTime? = OffsetDateTime.now()
)
