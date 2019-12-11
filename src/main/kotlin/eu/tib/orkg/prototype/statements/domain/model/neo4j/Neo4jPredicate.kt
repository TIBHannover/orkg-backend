package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.PredicateIdGraphAttributeConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.UUIDGraphAttributeConverter
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert
import java.util.UUID

@NodeEntity(label = "Predicate")
data class Neo4jPredicate(
    @Id
    @GeneratedValue
    private var id: Long? = null,

    @Property("label")
    @Required
    var label: String? = null,

    @Property("predicate_id")
    @Required
    @Convert(PredicateIdGraphAttributeConverter::class)
    private var predicateId: PredicateId? = null,

    @Property("created_by")
    @Convert(UUIDGraphAttributeConverter::class)
    var createdBy: UUID = UUID(0, 0)
) : AuditableEntity() {
    fun toPredicate() = Predicate(predicateId, label!!, createdAt!!, createdBy = createdBy)
}
