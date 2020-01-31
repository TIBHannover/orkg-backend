package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.escapeLiterals
import eu.tib.orkg.prototype.statements.application.rdf.RdfConstants
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.PredicateIdGraphAttributeConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.StatementIdGraphAttributeConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.UUIDGraphAttributeConverter
import org.neo4j.ogm.annotation.EndNode
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.RelationshipEntity
import org.neo4j.ogm.annotation.Required
import org.neo4j.ogm.annotation.StartNode
import org.neo4j.ogm.annotation.typeconversion.Convert
import java.util.UUID

@RelationshipEntity(type = "RELATED")
data class Neo4jStatement(
    @Id
    @GeneratedValue
    var id: Long? = null
) : AuditableEntity() {
    @StartNode
    @JsonIgnore
    var subject: Neo4jThing? = null

    @EndNode
    @JsonIgnore
    var `object`: Neo4jThing? = null

    @Property("statement_id")
    @Required
    @Convert(StatementIdGraphAttributeConverter::class)
    var statementId: StatementId? = null

    @Property("predicate_id")
    @Required
    @Convert(PredicateIdGraphAttributeConverter::class)
    var predicateId: PredicateId? = null

    @Property("created_by")
    @Convert(UUIDGraphAttributeConverter::class)
    var createdBy: UUID = UUID(0, 0)

    constructor(
        statementId: StatementId,
        subject: Neo4jThing,
        predicateId: PredicateId,
        `object`: Neo4jThing,
        createdBy: UUID = UUID(0, 0)
    ) :
        this(null) {
        this.statementId = statementId
        this.subject = subject
        this.predicateId = predicateId
        this.`object` = `object`
        this.createdBy = createdBy
    }

    override fun toString(): String {
        return "{id:$statementId}==(${subject!!.thingId} {${subject!!.label}})-[$predicateId]->(${`object`!!.thingId} {${`object`!!.label}})=="
    }

    /**
     * Convert the triple to a statement in NTriple format.
     */
    fun toNTriple(): String {
        val pPrefix = RdfConstants.PREDICATE_NS
        val result = "${serializeThing(subject!!)} <$pPrefix$predicateId> ${serializeThing(`object`!!)} ."
        if (result[0] == '"')
            // Ignore literal
            // TODO: log this somewhere
            return ""
        return result
    }

    private fun serializeThing(thing: Neo4jThing): String {
        val rPrefix = RdfConstants.RESOURCE_NS
        val pPrefix = RdfConstants.PREDICATE_NS
        val cPrefix = RdfConstants.CLASS_NS
        return when (thing) {
            is Neo4jResource -> "<$rPrefix${thing.thingId}>"
            is Neo4jPredicate -> "<$pPrefix${thing.thingId}>"
            is Neo4jClass -> "<$cPrefix${thing.thingId}>"
            else -> "\"${escapeLiterals(thing.label!!)}\"^^<http://www.w3.org/2001/XMLSchema#string>"
        }
    }
}
