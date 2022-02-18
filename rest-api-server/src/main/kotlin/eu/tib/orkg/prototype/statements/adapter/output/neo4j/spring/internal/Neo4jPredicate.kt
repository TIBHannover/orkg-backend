package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.AuditableEntity
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import eu.tib.orkg.prototype.statements.domain.model.toRdfModel
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.typeconversion.Convert

@NodeEntity(label = "Predicate")
data class Neo4jPredicate(
    @Id
    @GeneratedValue
    var id: Long? = null,

    @Property("label")
    @Required
    override var label: String? = null,

    @Property("predicate_id")
    @Required
    @Convert(PredicateIdConverter::class)
    private var predicateId: PredicateId? = null,

    @Property("created_by")
    @Convert(ContributorIdConverter::class)
    var createdBy: ContributorId = ContributorId.createUnknownContributor(),

    @Relationship(type = "RELATED", direction = Relationship.OUTGOING)
    @JsonIgnore
    var subjectOf: MutableSet<Neo4jStatement> = mutableSetOf()
) : Neo4jThing, AuditableEntity() {

    fun toPredicate(): Predicate {
        val pred = Predicate(predicateId, label!!, createdAt!!, createdBy = createdBy)
        pred.rdf = pred.toRdfModel() // TODO: not ideal, find a better way
        if (subjectOf.isNotEmpty())
            pred.description = subjectOf.firstOrNull { it.predicateId?.value == "description" }?.`object`?.label
        return pred
    }

    override val thingId: String?
        get() = predicateId?.value

    override fun toThing() = toPredicate()
}
