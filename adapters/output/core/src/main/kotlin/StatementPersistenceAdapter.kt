package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.id
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.toCypher
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.BY_OBJECT_ID
import eu.tib.orkg.prototype.statements.domain.model.neo4j.BY_SUBJECT_ID
import eu.tib.orkg.prototype.statements.domain.model.neo4j.MATCH_STATEMENT
import eu.tib.orkg.prototype.statements.domain.model.neo4j.MATCH_STATEMENT_WITH_LITERAL
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jLiteral
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatement
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementIdGenerator
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThingRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.RETURN_COUNT
import eu.tib.orkg.prototype.statements.domain.model.neo4j.RETURN_STATEMENT
import eu.tib.orkg.prototype.statements.domain.model.neo4j.WITH_SORTABLE_FIELDS
import eu.tib.orkg.prototype.statements.ports.StatementRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.Optional

@Component
class StatementPersistenceAdapter(
    private val resourcePersistenceAdapter: ResourcePersistenceAdapter,
    private val predicatePersistenceAdapter: PredicatePersistenceAdapter,
    private val literalPersistenceAdapter: LiteralPersistenceAdapter,
    private val client: Neo4jClient
): StatementRepository {

    override fun findAll(): Iterable<GeneralStatement> {
        val result = client
            .query("$MATCH_STATEMENT $RETURN_STATEMENT")
            .fetchAs<ProjectedStatement>()
            .all()
        return result.map { toStatement(it) }
    }

    override fun findAll(pagination: Pageable): Iterable<GeneralStatement> {
        val result = client
            .query("$MATCH_STATEMENT $RETURN_STATEMENT ${pagination.toCypher()}")
            .fetchAs<ProjectedStatement>()
            .all()
        return result.map { toStatement(it) }
    }

    override fun findById(statementId: StatementId): Optional<GeneralStatement> {
        val result = client
            .query("$MATCH_STATEMENT WHERE rel.statement_id = $id $RETURN_STATEMENT")
            .bind(statementId).to("id")
            .fetchAs<ProjectedStatement>()
            .one()
        return Optional.ofNullable(result).map { toStatement(it) }
    }

    override fun findAllBySubject(subjectId: String, pagination: Pageable): Page<GeneralStatement> {
        val query = "$MATCH_STATEMENT $BY_SUBJECT_ID $WITH_SORTABLE_FIELDS"
        val count = queryCount(query, pagination)
        val result = queryStatements(query, pagination)
        return PageImpl(result, pagination, count)
    }

    override fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Page<GeneralStatement> {
        val query = "$MATCH_STATEMENT WHERE rel.predicate_id = ${'$'}predicateId $WITH_SORTABLE_FIELDS"
        val parameters = mapOf("predicateId" to predicateId)
        val count = queryCount(query, pagination, parameters)
        val result = queryStatements(query, pagination, parameters)
        return PageImpl(result, pagination, count)
    }

    override fun findAllByObject(objectId: String, pagination: Pageable): Page<GeneralStatement> {
        val query = "$MATCH_STATEMENT $BY_OBJECT_ID $WITH_SORTABLE_FIELDS"
        val parameters = mapOf("id" to objectId)
        val count = queryCount(query, pagination, parameters)
        val result = queryStatements(query, pagination, parameters)
        return PageImpl(result, pagination, count)
    }

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> {
        val query =
            "$MATCH_STATEMENT WHERE (sub.`resource_id`=$id OR sub.`literal_id`=$id OR sub.`predicate_id`=$id OR sub.`class_id`=$id) AND rel.`predicate_id`={1} $WITH_SORTABLE_FIELDS"
        val parameters = mapOf("id" to subjectId)
        val count = queryCount(query, pagination, parameters)
        val result = queryStatements(query, pagination, parameters)
        return PageImpl(result, pagination, count)
    }

    override fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pagination: Pageable
    ): Page<GeneralStatement> {
        val query =
            "$MATCH_STATEMENT WHERE (obj.`resource_id`= $id OR obj.`literal_id`= $id OR obj.`predicate_id`= $id OR obj.`class_id`= $id) AND rel.`predicate_id`= ${'$'}predicateId $WITH_SORTABLE_FIELDS"
        val parameters = mapOf("id" to objectId, "predicateId" to predicateId)
        val count = queryCount(query, pagination, parameters)
        val result = queryStatements(query, pagination, parameters)
        return PageImpl(result, pagination, count)
    }

    override fun findAllByPredicateAndLabel(
        predicateId: PredicateId,
        literal: String,
        pagination: Pageable
    ): Page<GeneralStatement> {
        val query =
            "$MATCH_STATEMENT_WITH_LITERAL WHERE rel.`predicate_id`= ${'$'}predicateId AND obj.`label`= ${'$'}label $WITH_SORTABLE_FIELDS"
        val parameters = mapOf("predicateId" to predicateId, "label" to literal)
        val count = queryCount(query, pagination, parameters)
        val result = queryStatements(query, pagination, parameters)
        return PageImpl(result, pagination, count)
    }

    override fun findAllByPredicateAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ClassId,
        pagination: Pageable
    ): Page<GeneralStatement> {
        val query =
            "$MATCH_STATEMENT_WITH_LITERAL WHERE ${'$'}subjectClass IN labels(sub) AND rel.`predicate_id`= ${'$'}predicateId AND obj.`label`= ${'$'}label $WITH_SORTABLE_FIELDS"
        val parameters = mapOf("subjectClass" to subjectClass, "predicateId" to predicateId, "label" to literal)
        val count = queryCount(query, pagination, parameters)
        val result = queryStatements(query, pagination, parameters)
        return PageImpl(result, pagination, count)
    }

    private fun refreshObject(thing: Neo4jThing): Thing {
        return when (thing) {
            is Neo4jResource -> resourcePersistenceAdapter.findById(thing.resourceId).get()
            is Neo4jLiteral -> literalPersistenceAdapter.findById(thing.literalId).get()
            else -> thing.toThing()
        }
    }

    private fun toStatement(statement: ProjectedStatement) =
        GeneralStatement(
            id = statement.statementId!!,
            subject = refreshObject(statement.subject!!),
            predicate = predicatePersistenceAdapter.findById(statement.predicateId!!).get(),
            `object` = refreshObject(statement.`object`!!),
            createdAt = statement.createdAt!!,
            createdBy = statement.createdBy!!
        )

    private fun queryCount(query: String, pageable: Pageable, parameters: Map<String, Any> = emptyMap()): Long =
        client.query("$query $RETURN_COUNT ${pageable.toCypher()}")
            .bindAll(parameters)
            .fetchAs<Long>().one() ?: 0

    private fun queryStatements(
        query: String,
        pageable: Pageable,
        parameters: Map<String, Any> = emptyMap()
    ): List<GeneralStatement> =
        client.query("$query $RETURN_STATEMENT ${pageable.toCypher()}")
            .bindAll(parameters)
            .fetchAs<ProjectedStatement>()
            .all()
            .map { toStatement(it) }
}

internal data class ProjectedStatement(
    val statementId: StatementId?,
    val subject: Neo4jThing?,
    val predicateId: PredicateId?,
    val `object`: Neo4jThing?,
    val createdBy: ContributorId?,
    val createdAt: OffsetDateTime?
) {
    override fun toString(): String {
        return "{id:$statementId}==(${subject!!.thingId} {${subject!!.label}})-[$predicateId]->(${`object`!!.thingId} {${`object`!!.label}})=="
    }
}
