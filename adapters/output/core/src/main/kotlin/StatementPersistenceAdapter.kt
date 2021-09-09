package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.createdAt
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.createdBy
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.id
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.objectId
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.predicateId
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.statementId
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.subjectId
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.toCypher
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.MATCH_STATEMENT
import eu.tib.orkg.prototype.statements.domain.model.neo4j.MATCH_STATEMENT_WITH_LITERAL
import eu.tib.orkg.prototype.statements.domain.model.neo4j.RETURN_COUNT
import eu.tib.orkg.prototype.statements.ports.StatementRepository
import org.neo4j.driver.Record
import org.neo4j.driver.Value
import org.neo4j.driver.types.TypeSystem
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.stereotype.Component
import java.net.URI
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.UUID

internal val TYPE_LABELS = setOf("Class", "Literal", "Predicate", "Resource")

internal val WITH_MAPPED_VALUES = """
    |WITH sub, rel, obj,
    | [val in [sub.resource_id, sub.literal_id, sub.predicate_id, sub.class_id] where val is not null][0] AS subject_id,
    | labels(sub) AS subject_labels,
    | [val in [obj.resource_id, obj.literal_id, obj.predicate_id, obj.class_id] where val is not null][0] AS object_id,
    | labels(obj) AS object_labels,
    | rel.statement_id AS statement_id,
    | rel.predicate_id AS predicate_id,   
    | rel.created_at AS created_at,
    | rel.created_by AS created_by
    |""".trimMargin().replace("\n", "")

internal const val RETURN_STATEMENT_DATA =
    "RETURN statement_id, subject_id, subject_labels, predicate_id, object_id, object_labels, created_at, created_by"

typealias ThingLookupTable = Map<String, Thing>

@Component
class StatementPersistenceAdapter(
    private val resourcePersistenceAdapter: ResourcePersistenceAdapter,
    private val predicatePersistenceAdapter: PredicatePersistenceAdapter,
    private val literalPersistenceAdapter: LiteralPersistenceAdapter,
    private val classPersistenceAdapter: ClassPersistenceAdapter,
    private val client: Neo4jClient
) : StatementRepository {
    override fun save(statement: GeneralStatement) {
        val query = """
            MATCH (sub:Thing)
              WHERE sub.`resource_id`=$subjectId OR sub.`literal_id`=$subjectId OR sub.`predicate_id`=$subjectId OR sub.`class_id`=$subjectId
            MATCH (obj:Thing)
              WHERE obj.`resource_id`=$objectId  OR obj.`literal_id`=$objectId  OR obj.`predicate_id`=$objectId  OR obj.`class_id`=$objectId
            CREATE (sub)-[rel:RELATED {statement_id: $statementId, predicate_id: $predicateId, created_by: $createdBy, created_at: $createdAt}]->(obj)
        """.trimIndent()
        client.query(query)
            .bind(statement.subject.thingId.toString()).to("subjectId")
            .bind(statement.`object`.thingId.toString()).to("objectId")
            .bind(statement.predicate.id.toString()).to("predicateId")
            .bind(statement.id.toString()).to("statementId")
            .bind(statement.createdBy.toString()).to("createdBy")
            .bind(statement.createdAt?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)).to("createdAt")
            .run()
    }

    override fun delete(statement: GeneralStatement) {
        client.query("MATCH ()-[rel:RELATED]->() WHERE rel.statement_id = ${'$'}id DELETE rel")
            .bind(statement.id.toString()).to("id")
            .run()
    }

    override fun count(): Long = client
        .query("MATCH ()-[rel:RELATED]->() RETURN count(rel)")
        .fetchAs<Long>()
        .one() ?: 0

    override fun countByIdRecursive(id: String): Long {
        val query =
            """MATCH (p:`Thing`)-[*]->() WHERE p.`resource_id`=${'$'}id OR p.`literal_id`=${'$'}id OR p.`predicate_id`=${'$'}id OR p.`class_id`=${'$'}id RETURN COUNT(p)"""
        return client.query(query).fetchAs<Long>().one() ?: 0
    }

    override fun fetchAsBundle(rootId: String, configuration: Map<String, Any>): List<GeneralStatement> {
        val query = """
            MATCH (n:Thing)
            WHERE n.resource_id = $id OR n.literal_id = $id OR n.class_id = $id OR n.predicate_id = $id
            CALL apoc.path.subgraphAll(n, ${'$'}configuration)
            YIELD relationships
            UNWIND relationships as rel
            WITH startNode(rel) as sub, rel, endNode(rel) as obj
            $WITH_MAPPED_VALUES
            $RETURN_STATEMENT_DATA
            ORDER BY rel.created_at DESC
            """
        val result = client.query(query)
            .bind(configuration).to("configuration")
            .fetchAs(StatementData::class.java).mappedBy(statementDataMapper)
            .all()
        return if (result.isNotEmpty()) {
            val things = retrieveThings(idsToTypes(result))
            result.map { it.toGeneralStatement(things) }
        } else {
            emptyList()
        }
    }

    @Deprecated("This function has serious performance implications and should not be used!")
    override fun findAll(): Iterable<GeneralStatement> {
        val result = client
            .query("$MATCH_STATEMENT $WITH_MAPPED_VALUES $RETURN_STATEMENT_DATA")
            .fetchAs(StatementData::class.java).mappedBy(statementDataMapper)
            .all()
        return if (result.isNotEmpty()) {
            val things = retrieveThings(idsToTypes(result))
            result.map { it.toGeneralStatement(things) }
        } else {
            emptyList()
        }
    }

    // FIXME: should return a page?
    override fun findAll(pagination: Pageable): Iterable<GeneralStatement> {
        val result = client
            .query("$MATCH_STATEMENT $WITH_MAPPED_VALUES $RETURN_STATEMENT_DATA ${pagination.toCypher()}")
            .fetchAs(StatementData::class.java).mappedBy(statementDataMapper)
            .all()
        return if (result.isNotEmpty()) {
            val things = retrieveThings(idsToTypes(result))
            result.map { it.toGeneralStatement(things) }
        } else {
            emptyList()
        }
    }

    override fun findById(statementId: StatementId): Optional<GeneralStatement> {
        val result = client
            .query("$MATCH_STATEMENT $WITH_MAPPED_VALUES WHERE statement_id = $id $RETURN_STATEMENT_DATA")
            .bind(statementId.toString()).to("id")
            .fetchAs(StatementData::class.java).mappedBy(statementDataMapper)
            .one()

        return if (result.isPresent) {
            val things = retrieveThings(idsToTypes(listOf(result.get())))
            result.map { it.toGeneralStatement(things) }
        } else {
            Optional.empty<GeneralStatement>()
        }
    }

    override fun findAllBySubject(subjectId: String, pagination: Pageable): Page<GeneralStatement> {
        val query = "$MATCH_STATEMENT $WITH_MAPPED_VALUES WHERE subject_id = ${'$'}subjectId"
        val parameters = mapOf("subjectId" to subjectId)
        val count = queryCount(query, pagination, parameters)
        val result = queryStatements(query, pagination, parameters)
        return PageImpl(result, pagination, count)
    }

    override fun findAllByPredicate(predicateId: PredicateId, pagination: Pageable): Page<GeneralStatement> {
        val query = "$MATCH_STATEMENT $WITH_MAPPED_VALUES WHERE predicate_id = ${'$'}predicateId"
        val parameters = mapOf("predicateId" to predicateId.asString())
        val count = queryCount(query, pagination, parameters)
        val result = queryStatements(query, pagination, parameters)
        return PageImpl(result, pagination, count)
    }

    override fun findAllByObject(objectId: String, pagination: Pageable): Page<GeneralStatement> {
        val query = "$MATCH_STATEMENT $WITH_MAPPED_VALUES WHERE object_id = ${'$'}objectId"
        val parameters = mapOf("objectId" to objectId)
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
            "$MATCH_STATEMENT $WITH_MAPPED_VALUES WHERE subject_id = ${'$'}subjectId AND predicate_id = ${'$'}predicateId"
        val parameters = mapOf("subjectId" to subjectId, "predicateId" to predicateId.asString())
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
            "$MATCH_STATEMENT $WITH_MAPPED_VALUES WHERE object_id = ${'$'}objectId AND predicate_id = ${'$'}predicateId"
        val parameters = mapOf("objectId" to objectId, "predicateId" to predicateId.asString())
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
            "$MATCH_STATEMENT_WITH_LITERAL $WITH_MAPPED_VALUES WHERE predicate_id = ${'$'}predicateId AND obj.`label`= ${'$'}label"
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
            "$MATCH_STATEMENT_WITH_LITERAL $WITH_MAPPED_VALUES WHERE ${'$'}subjectClass IN labels(sub) AND predicate_id = ${'$'}predicateId AND obj.`label`= ${'$'}label"
        val parameters = mapOf("subjectClass" to subjectClass, "predicateId" to predicateId, "label" to literal)
        val count = queryCount(query, pagination, parameters)
        val result = queryStatements(query, pagination, parameters)
        return PageImpl(result, pagination, count)
    }

    private data class StatementData(
        val subjectId: String,
        val subjectLabels: List<String>,
        val objectId: String,
        val objectLabels: List<String>,
        val statementId: String,
        val predicateId: String,
        val createdBy: String,
        val createdAt: String,
    ) {
        val subjectType: String
            get() = subjectLabels.intersect(TYPE_LABELS).single()
        val objectType: String
            get() = objectLabels.intersect(TYPE_LABELS).single()

        fun toGeneralStatement(things: ThingLookupTable) =
            GeneralStatement(
                id = StatementId(this.statementId),
                subject = things[this.subjectId]!!,
                predicate = things[this.predicateId]!! as Predicate,
                `object` = things[this.objectId]!!,
                createdAt = OffsetDateTime.parse(this.createdAt),
                createdBy = ContributorId(this.createdBy),
            )
    }

    private fun retrieveThings(byType: Map<String, List<String>>): ThingLookupTable {
        val things: MutableMap<String, Thing> = HashMap()
        byType.forEach { (type, ids) ->
            if (ids.isNotEmpty()) {
                when (type) {
                    "Class" -> classPersistenceAdapter
                        .findAllById(ids.map(::ClassId))
                        .forEach { things[it.id.toString()] = it }
                    "Literal" -> literalPersistenceAdapter
                        .findAllById(ids.map(::LiteralId))
                        .forEach { things[it.id.toString()] = it }
                    "Predicate" -> predicatePersistenceAdapter
                        .findAllById(ids.map(::PredicateId))
                        .forEach { things[it.id.toString()] = it }
                    "Resource" -> resourcePersistenceAdapter
                        .findAllById(ids.map(::ResourceId))
                        .forEach { things[it.id.toString()] = it }
                    else -> throw IllegalStateException("Unable to fetch entities of unknown entity type $type.")
                }
            }
        }
        return things
    }

    private fun idsToTypes(statements: Collection<StatementData>): Map<String, List<String>> {
        val entities: MutableMap<String, MutableList<String>> = HashMap()
        statements.forEach { statement ->
            entities.getOrPut(statement.subjectType) { mutableListOf() }.add(statement.subjectId)
            entities.getOrPut(statement.objectType) { mutableListOf() }.add(statement.objectId)
            entities.getOrPut("Predicate") { mutableListOf() }.add(statement.predicateId)
        }
        return entities
    }

    private val statementDataMapper = { _: TypeSystem, record: Record ->
        StatementData(
            subjectId = record["subject_id"].asString(),
            subjectLabels = record["subject_labels"].asList(Value::asString),
            objectId = record["object_id"].asString(),
            objectLabels = record["object_labels"].asList(Value::asString),
            statementId = record["statement_id"].asString(),
            predicateId = record["predicate_id"].asString(),
            createdBy = record["created_by"].asString(),
            createdAt = record["created_at"].asString(),
        )
    }

    // TODO: find a good place. Something related to Thing?
    private fun Map<String, Any?>.toResource(): Resource =
        Resource(
            id = ResourceId(this["resource_id"] as String),
            label = this["label"] as String,
            createdAt = OffsetDateTime.parse(this["createdAt"] as String),
            createdBy = ContributorId(UUID.fromString(this["created_by"] as String)),
            //classes = (this["classes"] as List<String>).intersect(TYPE_LABELS).map(::ClassId).toSet()
            classes = setOf(ClassId("C1")) // FIXME
        )

    private fun Map<String, Any?>.toClass(): Class =
        Class(
            id = ClassId(this["class_id"] as String),
            label = this["label"] as String,
            uri = if (this["uri"] != null) URI.create(this["uri"] as String) else null,
            createdAt = null, // FIXME
            createdBy = ContributorId.createUnknownContributor(), // FIXME
        )

    private fun queryCount(query: String, pageable: Pageable, parameters: Map<String, Any> = emptyMap()): Long =
        client.query("$query $RETURN_COUNT ${pageable.toCypher()}")
            .bindAll(parameters)
            .fetchAs<Long>().one() ?: 0

    private fun queryStatements(
        query: String,
        pageable: Pageable,
        parameters: Map<String, Any> = emptyMap()
    ): List<GeneralStatement> {
        val result = client.query("$query $RETURN_STATEMENT_DATA ${pageable.toCypher()}")
            .bindAll(parameters)
            .fetchAs(StatementData::class.java).mappedBy(statementDataMapper)
            .all()
        return if (result.isNotEmpty()) {
            val things = retrieveThings(idsToTypes(result))
            result.map { it.toGeneralStatement(things) }
        } else {
            emptyList()
        }
    }
}
