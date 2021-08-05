package eu.tib.orkg.prototype.statements.domain.model.neo4j

import com.fasterxml.jackson.annotation.JsonIgnore
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.rdf.RdfConstants
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.PredicateIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.StatementIdConverter
import eu.tib.orkg.prototype.util.escapeLiterals
import org.springframework.data.neo4j.core.convert.ConvertWith
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.RelationshipProperties
import org.springframework.data.neo4j.core.schema.TargetNode

@RelationshipProperties
data class Neo4jStatement(
    @Id
    @GeneratedValue
    var id: Long? = null
) : AuditableEntity() {
    @TargetNode
    @JsonIgnore
    var `object`: Neo4jThing? = null

    @Property("statement_id")
    @ConvertWith(converter = StatementIdConverter::class)
    var statementId: StatementId? = null

    @Property("predicate_id")
    @ConvertWith(converter = PredicateIdConverter::class)
    var predicateId: PredicateId? = null

    @Property("created_by")
    @ConvertWith(converter = ContributorIdConverter::class)
    var createdBy: ContributorId = ContributorId.createUnknownContributor()

    constructor(
        statementId: StatementId,
        subject: Neo4jThing,
        predicateId: PredicateId,
        `object`: Neo4jThing,
        createdBy: ContributorId = ContributorId.createUnknownContributor()
    ) :
        this(null) {
        this.statementId = statementId
        this.predicateId = predicateId
        this.`object` = `object`
        this.createdBy = createdBy
    }

    override fun toString(): String {
        // FIXME: old code: return "{id:$statementId}==(${subject!!.thingId} {${subject!!.label}})-[$predicateId]->(${`object`!!.thingId} {${`object`!!.label}})=="
        return "{id:$statementId}==(?)-[$predicateId]->(${`object`!!.thingId} {${`object`!!.label}})=="
    }

    /**
     * Convert the triple to a statement in NTriple format.
     */
    fun toNTriple(): String {
        val pPrefix = RdfConstants.PREDICATE_NS
        // FIXME: old code: val result = "${serializeThing(subject!!)} <$pPrefix$predicateId> ${serializeThing(`object`!!)} ."
        val result = "? <$pPrefix$predicateId> ${serializeThing(`object`!!)} ."
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
