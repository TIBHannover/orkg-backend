package org.orkg.contenttypes.adapter.output.neo4j

import java.time.Clock
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Conditions
import org.neo4j.cypherdsl.core.Cypher.anonParameter
import org.neo4j.cypherdsl.core.Cypher.caseExpression
import org.neo4j.cypherdsl.core.Cypher.listOf
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Functions.collect
import org.neo4j.cypherdsl.core.Functions.count
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilder
import org.orkg.common.neo4jdsl.PagedQueryBuilder.fetchAs
import org.orkg.common.neo4jdsl.QueryCache
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResourceRepository
import org.orkg.graph.adapter.output.neo4j.toSortItems
import org.orkg.graph.adapter.output.neo4j.orElseGet
import org.orkg.graph.adapter.output.neo4j.toCondition
import org.orkg.graph.adapter.output.neo4j.where
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jRosettaStoneStatementAdapter(
    private val neo4jRepository: Neo4jResourceRepository,
    private val neo4jClient: Neo4jClient,
    private val clock: Clock = Clock.systemDefaultZone()
) : RosettaStoneStatementRepository {
    override fun nextIdentity(): ThingId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: ThingId
        do {
            id = ThingId(UUID.randomUUID().toString())
        } while (neo4jRepository.existsById(id))
        return id
    }

    override fun findByIdOrVersionId(id: ThingId): Optional<RosettaStoneStatement> {
        return neo4jClient.query("""
            CALL {
                MATCH (latest:RosettaStoneStatement:LatestVersion {id: ${'$'}id})
                RETURN latest
                UNION
                MATCH (latest:RosettaStoneStatement:LatestVersion)-[:VERSION]->(:RosettaStoneStatement:Version {id: ${'$'}id})
                RETURN latest
            }
            WITH latest
            OPTIONAL MATCH (latest)-[:CONTEXT]->(context:Thing)
            MATCH (latest)-[:VERSION]->(version:RosettaStoneStatement:Version)-[:METADATA]->(metadata:RosettaStoneStatementMetadata),
                  (latest)-[:TEMPLATE]->(template:RosettaNodeShape)
            MATCH (version)-[:SUBJECT]->(subjectNode:SubjectNode)-[:VALUE]->(subject:Thing)
            OPTIONAL MATCH (version)-[:OBJECT]->(objectNode:ObjectNode)-[:VALUE]->(object:Thing)
            WITH latest, context.id AS contextId, template.id AS templateId, metadata, version, object, COLLECT([subject, subjectNode.index]) AS subjects, objectNode
            WITH latest, contextId, templateId, metadata, version, CASE WHEN object IS NOT NULL THEN COLLECT([object, objectNode.index, objectNode.position]) ELSE [] END AS objects, subjects
            WITH latest, contextId, templateId, COLLECT([version, metadata, subjects, objects]) AS versions
            RETURN latest, contextId, templateId, versions""".trimIndent()
        )
            .bindAll(mapOf("id" to id.value))
            .fetchAs(RosettaStoneStatement::class.java)
            .mappedBy(RosettaStoneStatementMapper())
            .one()
    }

    override fun findAll(
        pageable: Pageable,
        context: ThingId?,
        templateId: ThingId?,
        templateTargetClassId: ThingId?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?
    ): Page<RosettaStoneStatement> = CypherQueryBuilder(neo4jClient, QueryCache.Uncached)
        .withCommonQuery {
            match(node("RosettaStoneStatement", "LatestVersion").named("latest"))
        }
        .withQuery { commonQuery ->
            val latest = node("RosettaStoneStatement", "LatestVersion").named("latest")
            val version = node("RosettaStoneStatement", "Version").named("version")
            val contextNode = node("Thing").named("context")
            val metadata = node("RosettaStoneStatementMetadata").named("metadata")
            val template = node("RosettaNodeShape").named("template")
            val subjectNode = node("SubjectNode").named("subjectNode")
            val subject = node("Thing").named("subject")
            val objectNode = node("ObjectNode").named("objectNode")
            val `object` = node("Thing").named("object")
            val contextId = name("contextId")
            val templateIdVar = name("templateId")
            val subjects = name("subjects")
            val objects = name("objects")
            val versions = name("versions")
            commonQuery.optionalMatch(latest.relationshipTo(contextNode, "CONTEXT"))
                .match(
                    latest.relationshipTo(version, "VERSION").relationshipTo(metadata, "METADATA"),
                    latest.relationshipTo(template, "TEMPLATE")
                )
                .match(version.relationshipTo(subjectNode, "SUBJECT").relationshipTo(subject, "VALUE"))
                .optionalMatch(version.relationshipTo(objectNode, "OBJECT").relationshipTo(`object`, "VALUE"))
                .let { match ->
                    templateTargetClassId?.let {
                        val targetClass = node("Class").withProperties("id", anonParameter(it.value))
                        match.match(latest.relationshipTo(targetClass, "INSTANCE_OF"))
                    } ?: match
                }
                .with(
                    latest,
                    contextNode,
                    template,
                    metadata,
                    version,
                    subject,
                    subjectNode,
                    `object`,
                    objectNode
                )
                .where(
                    context.toCondition { contextNode.property("id").eq(anonParameter(it.value)) },
                    templateId.toCondition { template.property("id").eq(anonParameter(it.value)) },
                    visibility.toCondition { filter ->
                        filter.targets.map { latest.property("visibility").eq(literalOf<String>(it.name)) }
                            .reduceOrNull(Condition::or) ?: Conditions.noCondition()
                    },
                    createdBy.toCondition { latest.property("created_by").eq(anonParameter(it.value.toString())) },
                    createdAtStart.toCondition { latest.property("created_at").gte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                    createdAtEnd.toCondition { latest.property("created_at").lte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                    observatoryId.toCondition { latest.property("observatory_id").eq(anonParameter(it.value.toString())) },
                    organizationId.toCondition { latest.property("organization_id").eq(anonParameter(it.value.toString())) }
                )
                .with(
                    latest,
                    contextNode.property("id").`as`(contextId),
                    template.property("id").`as`(templateIdVar),
                    metadata,
                    version,
                    `object`,
                    collect(listOf(subject.asExpression(), subjectNode.property("index"))).`as`(subjects),
                    objectNode
                )
                .with(
                    latest,
                    contextId,
                    templateIdVar,
                    metadata,
                    version,
                    // TODO: refactor query to workaround implicit aggregation key caused by case expression
                    caseExpression()
                        .`when`(`object`.isNotNull)
                        .then(collect(listOf(`object`.asExpression(), objectNode.property("index"), objectNode.property("position"))))
                        .elseDefault(listOf())
                        .`as`(objects),
                    subjects
                )
                .with(
                    latest,
                    contextId,
                    templateIdVar,
                    collect(listOf(version.asExpression(), metadata.asExpression(), subjects, objects)).`as`(versions)
                )
                // TODO: implement order by optimizations?
                .orderBy(
                    pageable.sort.orElseGet { Sort.by("created_at") }
                        .toSortItems(
                            propertyMappings = mapOf(
                                "id" to latest.property("id"),
                                "created_at" to latest.property("created_at"),
                                "created_by" to latest.property("created_by"),
                                "visibility" to latest.property("visibility"),
                                "template_id" to templateIdVar
                            ),
                            knownProperties = arrayOf("id", "created_at", "created_by", "visibility", "template_id")
                        )
                )
                .returning(latest.asExpression(), contextId, templateIdVar, versions)
        }
        .withCountQuery { commonQuery ->
            val latest = node("RosettaStoneStatement", "LatestVersion").named("latest")
            val contextNode = node("Thing").named("context")
            val template = node("RosettaNodeShape").named("template")
            commonQuery.optionalMatch(latest.relationshipTo(contextNode, "CONTEXT"))
                .match(latest.relationshipTo(template, "TEMPLATE"))
                .let { match ->
                    templateTargetClassId?.let {
                        val targetClass = node("Class").withProperties("id", anonParameter(it.value))
                        match.match(latest.relationshipTo(targetClass, "INSTANCE_OF"))
                    } ?: match
                }
                .with(
                    latest,
                    contextNode,
                    template,
                )
                .where(
                    context.toCondition { contextNode.property("id").eq(anonParameter(it.value)) },
                    templateId.toCondition { template.property("id").eq(anonParameter(it.value)) },
                    visibility.toCondition { filter ->
                        filter.targets.map { latest.property("visibility").eq(literalOf<String>(it.name)) }
                            .reduceOrNull(Condition::or) ?: Conditions.noCondition()
                    },
                    createdBy.toCondition { latest.property("created_by").eq(anonParameter(it.value.toString())) },
                    createdAtStart.toCondition { latest.property("created_at").gte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                    createdAtEnd.toCondition { latest.property("created_at").lte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                    observatoryId.toCondition { latest.property("observatory_id").eq(anonParameter(it.value.toString())) },
                    organizationId.toCondition { latest.property("organization_id").eq(anonParameter(it.value.toString())) }
                )
                .returning(count(latest))
        }
        .fetchAs<RosettaStoneStatement>()
        .mappedBy(RosettaStoneStatementMapper())
        .fetch(pageable, false)

    override fun save(statement: RosettaStoneStatement) {
        val versionInfo = neo4jClient.query("""
            MATCH (latest:RosettaStoneStatement:LatestVersion {id: ${'$'}id})
            OPTIONAL MATCH (latest)-[:VERSION]->(version:RosettaStoneStatement:Version)
            RETURN latest.version AS latestVersion, COUNT(version) AS versionCount""".trimIndent()
        )
            .bindAll(mapOf("id" to statement.id.value))
            .fetchAs<Pair<Long, Long>>()
            .mappedBy { _, record -> record["latestVersion"].asLong() to record["versionCount"].asLong() }
            .one()
        neo4jClient.query("""
            OPTIONAL MATCH (hlp:RosettaStoneStatement)
            WHERE hlp.id = ${'$'}__id__
            WITH hlp
            WHERE hlp IS NULL
            CREATE (latest:RosettaStoneStatement:LatestVersion:Resource:Thing {version: 0})
            WITH latest
            SET latest += ${'$'}__properties__
            SET latest:`${statement.templateTargetClassId}`
            WITH latest
            CALL {
                WITH latest
                UNWIND ${'$'}__labels__ AS label
                OPTIONAL MATCH (c:`Class` {id: label})
                WITH latest, c
                WHERE c IS NOT NULL
                CREATE (latest)-[rc:`INSTANCE_OF`]->(c)
                RETURN COUNT(latest) AS latest_instance_of_subquery_create
            }
            CALL {
                WITH latest
                MATCH (template:RosettaNodeShape {id: ${'$'}__templateId__})
                CREATE (latest)-[:TEMPLATE]->(template)
                RETURN COUNT(latest) AS template_subquery_create
            }
            CALL {
                WITH latest
                MATCH (context:Thing {id: ${'$'}__contextId__})
                WHERE context IS NOT NULL
                CREATE (latest)-[:CONTEXT]->(context)
                RETURN COUNT(latest) AS context_subquery_create
            }
            CALL {
                WITH latest
                UNWIND ${'$'}__versions__ AS __version__
                CREATE (latest)-[:VERSION]->(version:RosettaStoneStatement:Version:Resource:Thing)
                CREATE (version)-[:METADATA]->(metadata:RosettaStoneStatementMetadata)
                WITH __version__, version, metadata, latest
                SET version += __version__.__properties__
                SET metadata += __version__.__metadata__
                WITH latest, __version__, version
                CALL {
                    WITH latest, __version__, version
                    UNWIND __version__.__labels__ AS label
                    OPTIONAL MATCH (c:`Class` {id: label})
                    WITH latest, c
                    WHERE c IS NOT NULL
                    CREATE (version)-[rc:`INSTANCE_OF`]->(c)
                    RETURN COUNT(latest) AS version_instance_of_subquery_create
                }
                CALL {
                    WITH latest, __version__, version
                    UNWIND __version__.__subjects__ AS __subject__
                    MATCH (subject:Thing {id: __subject__.id})
                    CREATE (version)-[:SUBJECT]->(:SubjectNode {index: __subject__.index})-[:VALUE]->(subject)
                    RETURN COUNT(latest) AS subject_subquery_create
                }
                CALL {
                    WITH latest, __version__, version
                    UNWIND __version__.__objects__ AS __object__
                    MATCH (object:Thing {id: __object__.id})
                    CREATE (version)-[:OBJECT]->(:ObjectNode {index: __object__.index, position: __object__.position})-[:VALUE]->(object)
                    RETURN COUNT(latest) AS object_subquery_create
                }
                RETURN COUNT(latest) AS version_subquery_create
            }
            RETURN latest
            UNION
            MATCH (latest:RosettaStoneStatement)
            WHERE (latest.id = ${'$'}__id__ AND latest.version = ${'$'}__version__)
            WITH latest, (latest.version + 1) AS v
            SET latest.version = v
            WITH latest
            WHERE latest.version = (coalesce(${'$'}__version__, 0) + 1)
            SET latest += ${'$'}__properties__
            WITH latest
            CALL {
                WITH latest
                UNWIND ${'$'}__versions__ AS __version__
                CREATE (latest)-[:VERSION]->(version:RosettaStoneStatement:Version:Resource:Thing)
                CREATE (version)-[:METADATA]->(metadata:RosettaStoneStatementMetadata)
                WITH latest, __version__, version, metadata
                SET version += __version__.__properties__
                SET version:`${statement.templateTargetClassId}`
                SET metadata += __version__.__metadata__
                WITH latest, __version__, version
                CALL {
                    WITH latest, __version__, version
                    UNWIND __version__.__labels__ AS label
                    OPTIONAL MATCH (c:`Class` {id: label})
                    WITH latest, c
                    WHERE c IS NOT NULL
                    CREATE (version)-[rc:`INSTANCE_OF`]->(c)
                    RETURN COUNT(latest) AS version_instance_of_subquery_update
                }
                CALL {
                    WITH latest, __version__, version
                    UNWIND __version__.__subjects__ AS __subject__
                    MATCH (subject:Thing {id: __subject__.id})
                    CREATE (version)-[:SUBJECT]->(:SubjectNode {index: __subject__.index})-[:VALUE]->(subject)
                    RETURN COUNT(latest) AS subject_subquery_update
                }
                CALL {
                    WITH latest, __version__, version
                    UNWIND __version__.__objects__ AS __object__
                    MATCH (object:Thing {id: __object__.id})
                    CREATE (version)-[:OBJECT]->(:ObjectNode {index: __object__.index, position: __object__.position})-[:VALUE]->(object)
                    RETURN COUNT(latest) AS object_subquery_update
                }
                RETURN COUNT(latest) AS version_subquery_update
            }
            RETURN latest""".trimIndent()
        )
            .bindAll(
                mapOf(
                    "__id__" to statement.id.value,
                    "__labels__" to listOf("RosettaStoneStatement", "LatestVersion", statement.templateTargetClassId.value),
                    "__version__" to (versionInfo?.first ?: 1L),
                    "__properties__" to mapOf(
                        "id" to statement.id.value,
                        "label" to statement.label,
                        "created_by" to statement.versions.first().createdBy.toString(),
                        "created_at" to statement.versions.first().createdAt.format(ISO_OFFSET_DATE_TIME),
                        "observatory_id" to (statement.observatories.singleOrNull() ?: ObservatoryId.UNKNOWN).value.toString(),
                        "organization_id" to (statement.organizations.singleOrNull() ?: OrganizationId.UNKNOWN).value.toString(),
                        "extraction_method" to statement.extractionMethod.name,
                        "verified" to false,
                        "visibility" to statement.visibility.name,
                        "unlisted_by" to statement.unlistedBy?.value?.toString(),
                        "modifiable" to statement.modifiable
                    ),
                    "__contextId__" to statement.contextId?.value,
                    "__templateId__" to statement.templateId.value,
                    "__versions__" to statement.versions.drop(versionInfo?.second?.toInt() ?: 0)
                        .mapIndexed { index, version ->
                            mapOf(
                                "__labels__" to listOf("RosettaStoneStatement", "Version", statement.templateTargetClassId.value),
                                "__properties__" to mapOf(
                                    "id" to version.id.value,
                                    "label" to statement.label,
                                    "created_by" to version.createdBy.toString(),
                                    "created_at" to version.createdAt.format(ISO_OFFSET_DATE_TIME),
                                    "observatory_id" to (version.observatories.singleOrNull() ?: ObservatoryId.UNKNOWN).value.toString(),
                                    "organization_id" to (version.organizations.singleOrNull() ?: OrganizationId.UNKNOWN).value.toString(),
                                    "extraction_method" to version.extractionMethod.name,
                                    "verified" to false,
                                    "visibility" to version.visibility.name,
                                    "unlisted_by" to version.unlistedBy?.value?.toString(),
                                    "modifiable" to version.modifiable,
                                ),
                                "__subjects__" to version.subjects.mapIndexed { idx, subject ->
                                    mapOf(
                                        "index" to idx,
                                        "id" to subject.id.value
                                    )
                                },
                                "__objects__" to version.objects.flatMapIndexed { position, objects ->
                                    objects.mapIndexed { idx, it ->
                                        mapOf(
                                            "position" to position,
                                            "index" to idx,
                                            "id" to it.id.value
                                        )
                                    }
                                },
                                "__metadata__" to mapOf(
                                    "formatted_label" to version.formattedLabel.value,
                                    "certainty" to version.certainty.name,
                                    "negated" to version.negated,
                                    "version" to index + (versionInfo?.second?.toInt() ?: 0),
                                    "object_count" to version.objects.size,
                                    "deleted_by" to version.deletedBy?.toString(),
                                    "deleted_at" to version.deletedAt?.format(ISO_OFFSET_DATE_TIME)
                                )
                            )
                        }
                )
            )
            .run()
    }

    override fun deleteAll() {
        neo4jClient.query("""
            MATCH (latest:RosettaStoneStatement:LatestVersion)
            OPTIONAL MATCH (latest)-[:CONTEXT]->(context:Thing)
            MATCH (latest)-[:VERSION]->(version:RosettaStoneStatement:Version)-[:METADATA]->(metadata:RosettaStoneStatementMetadata),
                  (latest)-[:TEMPLATE]->(template:RosettaNodeShape)
            MATCH (version)-[:SUBJECT]->(subjectNode:SubjectNode)-[:VALUE]->(subject:Thing)
            OPTIONAL MATCH (version)-[:OBJECT]->(objectNode:ObjectNode)-[:VALUE]->(object:Thing)
            DETACH DELETE latest, version, metadata, subject, subjectNode, object, objectNode""".trimIndent()
        ).run()
    }

    override fun softDelete(id: ThingId, contributorId: ContributorId) {
        neo4jClient.query("""
            MATCH (latest:RosettaStoneStatement:LatestVersion {id: ${'$'}id}),
                  (latest)-[:VERSION]->(version:RosettaStoneStatement:Version)-[:METADATA]->(metadata:RosettaStoneStatementMetadata),
                  (latest)-[:TEMPLATE]->(template:RosettaNodeShape)
            SET latest.visibility = 'DELETED',
                version.visibility = 'DELETED',
                metadata.deleted_by = ${'$'}deleted_by,
                metadata.deleted_at = ${'$'}deleted_at""".trimIndent()
        )
            .bindAll(
                mapOf(
                    "id" to id.value,
                    "deleted_by" to contributorId.toString(),
                    "deleted_at" to OffsetDateTime.now(clock).format(ISO_OFFSET_DATE_TIME)
                )
            )
            .run()
    }

    override fun delete(id: ThingId) {
        neo4jClient.query("""
            MATCH (latest:RosettaStoneStatement:LatestVersion {id: ${'$'}id})
            OPTIONAL MATCH (latest)-[:CONTEXT]->(context:Thing)
            MATCH (latest)-[:VERSION]->(version:RosettaStoneStatement:Version)-[:METADATA]->(metadata:RosettaStoneStatementMetadata),
                  (latest)-[:TEMPLATE]->(template:RosettaNodeShape)
            MATCH (version)-[:SUBJECT]->(subjectNode:SubjectNode)-[:VALUE]->(Thing)
            OPTIONAL MATCH (version)-[:OBJECT]->(objectNode:ObjectNode)-[:VALUE]->(Thing)
            DETACH DELETE latest, version, metadata, subjectNode, objectNode""".trimIndent()
        )
            .bindAll(mapOf("id" to id.value))
            .run()
    }

    override fun isUsedAsObject(id: ThingId): Boolean =
        neo4jClient.query("""
            CALL {
                MATCH (latest:RosettaStoneStatement:LatestVersion {id: ${'$'}id})
                RETURN latest
                UNION
                MATCH (latest:RosettaStoneStatement:LatestVersion)-[:VERSION]->(:RosettaStoneStatement:Version {id: ${'$'}id})
                RETURN latest
            }
            MATCH (latest)-[:VERSION]->(version:RosettaStoneStatement:Version)
            WITH latest.id AS latestId, COLLECT(version.id) AS ids
            WITH (latestId + ids) AS ids
            UNWIND ids AS id
            CALL {
                WITH id
                MATCH (:Thing {id: id})<-[r:RELATED]-()
                RETURN r
                UNION ALL
                WITH id
                MATCH (:Thing {id: id})<-[r:VALUE]-()
                RETURN r
            }
            WITH r
            RETURN COUNT(r) > 0 AS count""".trimIndent()
        )
            .bindAll(mapOf("id" to id.value))
            .fetchAs<Boolean>()
            .one() ?: false
}
