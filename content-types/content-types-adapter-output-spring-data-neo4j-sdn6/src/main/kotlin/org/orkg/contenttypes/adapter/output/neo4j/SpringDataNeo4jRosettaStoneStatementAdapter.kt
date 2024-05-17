package org.orkg.contenttypes.adapter.output.neo4j

import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResourceRepository
import org.orkg.graph.output.PredicateRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jRosettaStoneStatementAdapter(
    private val predicateRepository: PredicateRepository,
    private val neo4jRepository: Neo4jResourceRepository,
    private val neo4jClient: Neo4jClient
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
            .mappedBy(RosettaStoneStatementMapper(predicateRepository))
            .one()
    }

    override fun findAll(pageable: Pageable): Page<RosettaStoneStatement> {
        val contents = neo4jClient.query("""
            MATCH (latest:RosettaStoneStatement:LatestVersion)
            OPTIONAL MATCH (latest)-[:CONTEXT]->(context:Thing)
            MATCH (latest)-[:VERSION]->(version:RosettaStoneStatement:Version)-[:METADATA]->(metadata:RosettaStoneStatementMetadata),
                  (latest)-[:TEMPLATE]->(template:RosettaNodeShape)
            MATCH (version)-[:SUBJECT]->(subjectNode:SubjectNode)-[:VALUE]->(subject:Thing)
            OPTIONAL MATCH (version)-[:OBJECT]->(objectNode:ObjectNode)-[:VALUE]->(object:Thing)
            WITH latest, context.id AS contextId, template.id AS templateId, metadata, version, object, COLLECT([subject, subjectNode.index]) AS subjects, objectNode
            WITH latest, contextId, templateId, metadata, version, CASE WHEN object IS NOT NULL THEN COLLECT([object, objectNode.index, objectNode.position]) ELSE [] END AS objects, subjects
            WITH latest, contextId, templateId, COLLECT([version, metadata, subjects, objects]) AS versions
            RETURN latest, contextId, templateId, versions
            SKIP ${'$'}skip
            LIMIT ${'$'}limit""".trimIndent()
        )
            .bindAll(mapOf("skip" to pageable.offset, "limit" to pageable.pageSize))
            .fetchAs(RosettaStoneStatement::class.java)
            .mappedBy(RosettaStoneStatementMapper(predicateRepository))
            .all()
            .toList()
        val count = neo4jClient.query("""MATCH (latest:RosettaStoneStatement:LatestVersion) RETURN COUNT(latest)""")
            .fetchAs<Long>()
            .one() ?: 0
        return PageImpl(contents, pageable, count)
    }

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
                                    "certainty" to version.certainty.name,
                                    "negated" to version.negated,
                                    "version" to index + (versionInfo?.second?.toInt() ?: 0)
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
}
