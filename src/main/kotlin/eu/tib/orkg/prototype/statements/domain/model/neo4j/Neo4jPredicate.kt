package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import org.neo4j.ogm.annotation.*

@NodeEntity(label = "Predicate")
data class Neo4jPredicate(
    @Id
    @GeneratedValue
    private var id: Long? = null,

    @Property("label")
    @Required
    private var label: String?
) {
    fun toPredicate(): Predicate {
        val id = id
        val label = label
        if (id == null || label == null)
            throw IllegalStateException("This should never happen!")
        return Predicate(PredicateId(id), label)
    }
}
