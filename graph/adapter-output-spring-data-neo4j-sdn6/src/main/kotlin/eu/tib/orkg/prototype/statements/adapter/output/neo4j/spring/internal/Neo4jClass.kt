package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.net.URI
import java.time.OffsetDateTime
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property

@Node("Class")
class Neo4jClass : Neo4jThing() {
    @Property("class_id")
    var classId: ClassId? = null

    @Property("uri")
    var uri: String? = null

    @Property("created_by")
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    @Property("created_at")
    var createdAt: OffsetDateTime? = null

    fun toClass(statementRepository: StatementRepository) = Class(
        id = ThingId(classId!!.value),
        label = label!!,
        uri = if (uri != null) URI.create(uri!!) else null,
        createdAt = createdAt!!,
        createdBy = createdBy,
        description = statementRepository.findClassDescription(ThingId(classId!!.value)).orElse(null)
    )

    override val thingId: String?
        get() = classId?.value

    override fun toThing(statementRepository: StatementRepository) = toClass(statementRepository)
}
