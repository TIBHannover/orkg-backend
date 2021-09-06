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
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.BY_OBJECT_ID
import eu.tib.orkg.prototype.statements.domain.model.neo4j.BY_SUBJECT_ID
import eu.tib.orkg.prototype.statements.domain.model.neo4j.MATCH_STATEMENT
import eu.tib.orkg.prototype.statements.domain.model.neo4j.MATCH_STATEMENT_WITH_LITERAL
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClass
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jLiteral
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThing
import eu.tib.orkg.prototype.statements.domain.model.neo4j.RETURN_COUNT
import eu.tib.orkg.prototype.statements.domain.model.neo4j.WITH_SORTABLE_FIELDS
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.ContributorIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.OffsetDateTimeConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.PredicateIdConverter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.mapping.StatementIdConverter
import eu.tib.orkg.prototype.statements.ports.StatementRepository
import org.neo4j.driver.internal.InternalNode
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.convert.ConvertWith
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.stereotype.Component
import java.net.URI
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.Optional
import java.util.UUID
import java.util.logging.Logger


@Component
class StatementPersistenceAdapter(
    private val resourcePersistenceAdapter: ResourcePersistenceAdapter,
    private val predicatePersistenceAdapter: PredicatePersistenceAdapter,
    private val literalPersistenceAdapter: LiteralPersistenceAdapter,
    private val client: Neo4jClient
): StatementRepository {

    private val logger = Logger.getLogger("StatementPersistenceAdpater")

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
        logger.info("Deleting ID: ${statement.id?.value}")
        client.query("MATCH ()->[rel:RELATED]->() WHERE rel.statement_id = ${'$'}id DELETE rel")
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
            RETURN startNode(rel) as subject, rel as predicate, endNode(rel) as object
            ORDER BY rel.created_at DESC
            """
        return client.query(query).fetch().all().map { extractStatement(it.toMap() as HashMap<String, Any>) }
    }

    override fun findAll(): Iterable<GeneralStatement> {
        val result = client
            .query("$MATCH_STATEMENT $RETURN_STATEMENT")
            .fetch()
            .all()
        logger.info("Size 1: ${result.size}")
        return result.map { extractStatement(it.toMap() as HashMap<String, Any>) }
    }

    override fun findAll(pagination: Pageable): Iterable<GeneralStatement> {
        logger.info("Finding all results")
        //val tempRes = client.query("$MATCH_STATEMENT $RETURN_STATEMENT").fetch().all()
        //logger.info("Res: $tempRes")
        var result = client
            .query("$MATCH_STATEMENT $RETURN_STATEMENT ${pagination.toCypher()}")
            //.query("$MATCH_STATEMENT $RETURN_STATEMENT")
            //.fetchAs<ProjectedStatement>()
            .fetch()
            .all()


        logger.info("size:${result.size}")
        return result.map {
            extractStatement(it.toMap() as HashMap<String, Any>)
        }

        //return result.map {   toStatement(it)    }
    }

    override fun findById(statementId: StatementId): Optional<GeneralStatement> {
        val result = client
            .query("$MATCH_STATEMENT WHERE rel.statement_id = $id $RETURN_STATEMENT")
            .bind(statementId.toString()).to("id")
            .fetch()
            .one()
        //logger.info("Find By ID: ${result?.statementId?.value}")
        return Optional.ofNullable(result).map { extractStatement(result.get().toMap() as HashMap<String, Any>) }
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
        logger.info("Inside refreshObject: $thing")
        return when (thing) {
            is Neo4jResource -> resourcePersistenceAdapter.findById(thing.resourceId).get()
            is Neo4jLiteral -> literalPersistenceAdapter.findById(thing.literalId).get()
            else -> thing.toThing()
        }
    }

    private fun toStatement(statement: ProjectedStatement) =
        GeneralStatement(
            id = statement.statementId,
            subject = refreshObject(statement.subject),
            predicate = predicatePersistenceAdapter.findById(statement.predicateId).get(),
            `object` = refreshObject(statement.`object`),
            createdAt = statement.createdAt,
            createdBy = statement.createdBy
        )

    private fun extractStatement(statement: Map<String, Any?>): GeneralStatement {
        //Logic based on LABELS of Subject & Object -> call when and add your logic
        //in else of when -> throw an error
        //Collection of an unmodifiable map
        logger.info("ID:${statement.get("statementId")}")
        logger.info("Subject: ${statement.get("subject")}")
        logger.info("Predicate: ${statement.get("predicateId")}")
        logger.info("Object: ${statement.get("object")}")

        //Normal behavior
        val sub = statement.get("subject") as InternalNode
        val subjectMap = sub.asMap() //Construct the object ourselves ->
        logger.info("Sub Node:${sub.asMap()}")

        var subLabels = statement.get("subLabels") as List<String>
        var objLabels = statement.get("objLabels") as List<String>

        subLabels = subLabels.filter { !it.toLowerCase().equals("thing") && !it.toLowerCase().equals("auditableentity") }

        logger.info("Subject Labels")
        subLabels.map {
            logger.info(it)
        }



        objLabels = objLabels.filter { !it.toLowerCase().equals("thing") && !it.toLowerCase().equals("auditableentity") }
        logger.info("Object Labels")
        objLabels.map {
            logger.info(it)
        }

        var subRes: Any = ""

        /*var subjectResult = when {
            subLabels.contains("Class") -> {
                Neo4jClass(
                    label = subjectMap.get("label").toString(),
                    classId = ClassId(subjectMap.get("class_id").toString()),
                    uri = subjectMap.get("uri") as URI?,
                    createdBy = ContributorId(subjectMap.get("created_by").toString())
                )
            }
            else -> {
                Neo4jResource(
                    label = subjectMap.get("label").toString(),
                    resourceId = ResourceId(subjectMap.get("resource_id").toString()),
                )
            }
        }*/



        val tempSubject = subjectMap.get("class_id")

        when(tempSubject){
            null -> subRes = Neo4jResource(
                label = subjectMap.get("label").toString(),
                resourceId = ResourceId(subjectMap.get("resource_id").toString()),
            )
            else -> {
                subRes = Neo4jClass(
                    label = subjectMap.get("label").toString(),
                    classId = ClassId(subjectMap.get("class_id").toString()),
                    uri = subjectMap.get("uri") as URI?,
                    createdBy = ContributorId(subjectMap.get("created_by").toString())
                )
            }

        }


        val obj = statement.get("object") as InternalNode
        val objectMap = obj.asMap()
        logger.info("Obj Node:${obj.asMap()}")

        val objectResource = Neo4jResource(label = objectMap.get("label").toString(),
            resourceId = ResourceId(objectMap.get("resource_id").toString()),
        )

        var objRes : Any = ""

        val tempObject = objectMap.get("datatype")

        val uuid = UUID.randomUUID()
        var createdBy = ContributorId(uuid.toString())

        if(statement.get("createdBy") != null) {
            createdBy = ContributorId(statement.get("createdBy") as String)
        }

        /*var objectResult = when {
            objLabels.contains("Literal") -> {
                Neo4jLiteral(
                    label = objectMap.get("label").toString(),
                    literalId = LiteralId(objectMap.get("literal_id").toString()),
                    datatype = objectMap.get("datatype").toString(),
                    createdBy = createdBy
                )
            }
            else -> {
                Neo4jClass(
                    label = objectMap.get("label").toString(),
                    classId = ClassId(objectMap.get("class_id").toString()),
                    uri = objectMap.get("uri") as URI?,
                    createdBy = createdBy
                )

            }
        }*/

        when(tempObject){
            null -> {
                objRes = Neo4jClass(
                    label = objectMap.get("label").toString(),
                    classId = ClassId(objectMap.get("class_id").toString()),
                    uri = objectMap.get("uri") as URI?,
                    createdBy = ContributorId(objectMap.get("created_by").toString())
                )
            }
            else -> {
                objRes = Neo4jLiteral(
                    label = objectMap.get("label").toString(),
                    literalId = LiteralId(objectMap.get("literal_id").toString()),
                    datatype = objectMap.get("datatype").toString(),
                    createdBy = ContributorId(objectMap.get("created_by").toString())
                )
            }
        }



        var subjectThing: Any = ""

        val subClassId = subjectMap.get("class_id")

        when(subClassId){
            null -> {
                subjectThing = refreshObject(subRes as Neo4jResource)
            }
            else -> {
                subjectThing = refreshObject(subRes as Neo4jClass)
            }
        }

        var objectThing: Any = ""

        val objectDataType = objectMap.get("datatype")

        when(objectDataType){
            null ->
                objectThing = refreshObject(objRes as Neo4jClass)
            else -> {
                objectThing = refreshObject(objRes as Neo4jLiteral)
            }
            //could also be resource or predicate
            // get list of node labels and then find which type it is
        }
        logger.info("Subject Thing: $subjectThing")
        logger.info("Object Thing: $objectThing")

        return GeneralStatement(
            id = StatementId(statement.get("statementId") as String),
            subject = subjectThing,
            predicate = predicatePersistenceAdapter.findById(PredicateId(statement.get("predicateId") as String))
                .get(),
            `object` = objectThing,
            createdAt = OffsetDateTime.now(),
            createdBy = createdBy
        )
    }

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
            .fetch()
            .all()
            .map { extractStatement(it.toMap() as HashMap<String, Any>) }
}


//value -> THING / Literal
//THING -> real RDF Resource -> class OR predicates
//sealed classes for both
//sealed interfaces for each statement
//1 INTerface-1 : Value
//2 INTErface 2: Thing

internal data class ProjectedStatement(
    @ConvertWith(converter = StatementIdConverter::class)
    val statementId: StatementId,
    val subject: Neo4jThing,
    @ConvertWith(converter = PredicateIdConverter::class)
    val predicateId: PredicateId,
    val `object`: Neo4jThing,
    @ConvertWith(converter = ContributorIdConverter::class)
    val createdBy: ContributorId,
    @ConvertWith(converter = OffsetDateTimeConverter::class)
    val createdAt: OffsetDateTime
) {
    override fun toString(): String {
        return "{id:$statementId}==(${subject.thingId} {${subject.label}})-[$predicateId]->(${`object`.thingId} {${`object`.label}})=="
    }
}

// Needs to match properties of ProjectedStatement exactly
private const val RETURN_STATEMENT = """RETURN rel, sub AS subject, obj AS object, rel.statement_id AS statementId, rel.predicate_id AS predicateId, rel.created_at AS createdAt, rel.created_by AS createdBy, labels(sub) AS subLabels, labels(obj) AS objLabels"""
