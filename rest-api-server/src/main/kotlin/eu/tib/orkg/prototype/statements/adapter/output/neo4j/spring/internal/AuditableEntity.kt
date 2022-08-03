package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import java.time.OffsetDateTime
import org.neo4j.ogm.annotation.Property

open class AuditableEntity(
    @Property("created_at")
    var createdAt: OffsetDateTime? = OffsetDateTime.now()
)
