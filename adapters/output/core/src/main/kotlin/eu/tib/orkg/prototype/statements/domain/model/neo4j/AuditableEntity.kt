package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.OffsetDateTimeConverter
import org.springframework.data.neo4j.core.convert.ConvertWith
import org.springframework.data.neo4j.core.schema.Property
import java.time.OffsetDateTime

open class AuditableEntity(
    @Property("created_at")
    @ConvertWith(converter = OffsetDateTimeConverter::class)
    var createdAt: OffsetDateTime? = OffsetDateTime.now()
)
