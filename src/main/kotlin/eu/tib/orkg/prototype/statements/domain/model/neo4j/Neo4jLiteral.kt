package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.*
import eu.tib.orkg.prototype.statements.domain.model.*
import org.neo4j.ogm.annotation.*

@NodeEntity(label = "Literal")
data class Neo4jLiteral(
    @Id
    @GeneratedValue
    var id: Long? = null
) {
    @Property("label")
    @Required
    var label: String? = null

    @Relationship(type = "HAS_VALUE_OF")
    @JsonIgnore
    var resources: MutableSet<Neo4jStatementWithLiteral> = mutableSetOf()

    constructor(id: Long? = null, label: String) : this(id) {
        this.label = label
    }

    fun toLiteral() = Literal(LiteralId(id!!), label!!)

    fun toObject() =
        LiteralObject(LiteralId(id!!), label!!)
}
