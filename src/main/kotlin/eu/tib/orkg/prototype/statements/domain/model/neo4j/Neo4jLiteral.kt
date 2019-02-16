package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.*
import eu.tib.orkg.prototype.statements.domain.model.*
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.*
import org.neo4j.ogm.annotation.*
import org.neo4j.ogm.annotation.typeconversion.*

@NodeEntity(label = "Literal")
data class Neo4jLiteral(
    @Id
    @GeneratedValue
    var id: Long? = null,

    @Property("label")
    @Required
    var label: String? = null,

    @Property("literal_id")
    @Required
    @Convert(LiteralIdGraphAttributeConverter::class)
    var literalId: LiteralId? = null,

    @Relationship(type = "HAS_VALUE_OF")
    @JsonIgnore
    var resources: MutableSet<Neo4jStatementWithLiteral> = mutableSetOf()
) {
    fun toLiteral() = Literal(literalId, label!!)

    fun toObject() = LiteralObject(literalId, label!!)
}
