package org.orkg

import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.function.Function
import kotlin.collections.List
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.wherePredicate
import org.orkg.contenttypes.output.ComparisonPublishedRepository
import org.orkg.contenttypes.output.ComparisonTableRepository
import org.orkg.contenttypes.output.VisualizationDataRepository
import org.orkg.graph.adapter.output.neo4j.toResource
import org.orkg.graph.adapter.output.neo4j.toThingId
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.stereotype.Component
import org.orkg.graph.domain.List as OrkgList

private val firstBundleConfig = BundleConfiguration(
    minLevel = null,
    maxLevel = 3,
    blacklist = listOf(
        Classes.researchField,
        Classes.contribution,
        Classes.visualization,
        Classes.sustainableDevelopmentGoal
    ),
    whitelist = emptyList()
)
private val secondBundleConfig = BundleConfiguration(
    minLevel = null,
    maxLevel = 1,
    blacklist = emptyList(),
    whitelist = listOf(
        Classes.researchField,
        Classes.contribution,
        Classes.visualization,
        Classes.comparisonRelatedFigure,
        Classes.comparisonRelatedResource,
        Classes.sustainableDevelopmentGoal
    )
)

@Component
@Profile("comparisonMigrations")
@Order(2)
class ComparisonMigrationRunner(
    private val listRepository: ListRepository,
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val statementService: StatementUseCases,
    private val literalRepository: LiteralRepository,
    private val comparisonPublishedRepository: ComparisonPublishedRepository,
    private val comparisonTableRepository: ComparisonTableRepository,
    private val visualizationDataRepository: VisualizationDataRepository,
    private val neo4jClient: Neo4jClient
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun run(args: ApplicationArguments?) {
        logger.info("Starting comparison migration...")

        if (!resourceRepository.findAll(PageRequests.SINGLE, includeClasses = setOf(Classes.comparisonPublished)).isEmpty) {
            logger.info("Skipping comparison migration because an instance of 'ComparisonPublished' already exists.")
            return
        }

        val versions = findAllComparisonVersions()
        migrateAllComparisonLabels()
        versions.forEach(::createHeadComparison)
        deleteDuplicateVersionStatements()
        addLatestVersionLabel()

        logger.info("Comparison migration complete")
    }

    private fun findAllComparisonVersions() =
        neo4jClient.query("""
            MATCH (c:Comparison)<-[r:RELATED*0.. {predicate_id: "hasPreviousVersion"}]-(v:Comparison)
            WHERE NOT EXISTS((c)-[:RELATED {predicate_id: "hasPreviousVersion"}]->(:Comparison))
            WITH c, COLLECT([v, LAST(r), endNode(LAST(r)).id]) AS versions
            RETURN versions
            """.trimIndent()
        )
            .fetchAs<VersionSubgraph>()
            .mappedBy { _, record ->
                val versions = record.get("versions").asList { version ->
                    val pair = version.asList(Function.identity())
                    val comparison = pair[0].asNode().toResource()
                    val hasPreviousVersion = pair[1].let { if (it.isNull) null else it.asRelationship() }
                    comparison to hasPreviousVersion?.let {
                        ObjectRelation(
                            id = StatementId(it["statement_id"].asString()),
                            objectId = pair[2].toThingId()!!
                        )
                    }
                }
                VersionSubgraph(versions.toMap())
            }
            .all()

    private fun migrateAllComparisonLabels() {
        neo4jClient.query("""
            MATCH (c:Comparison)
            SET c:ComparisonPublished
            REMOVE c:Comparison
            RETURN COUNT(c)
            """.trimIndent()
        ).run()
        neo4jClient.query("""
            MATCH (c:ComparisonPublished)-[r:INSTANCE_OF]->(:Class {id: "Comparison"})
            WITH c, COLLECT(r) AS rs
            MATCH (cp:Class {id: "ComparisonPublished"})
            WITH c, cp, rs
            UNWIND rs AS r
            CALL apoc.refactor.to(r, cp)
            YIELD input, output
            RETURN COUNT(c)
            """.trimIndent()
        ).run()
    }

    private fun deleteDuplicateVersionStatements() {
        neo4jClient.query("""
            MATCH (c:Comparison)-[r:RELATED {predicate_id: "hasPublishedVersion"}]->(p:ComparisonPublished)
            WITH c.id AS cid, p.id AS pid, COLLECT(r) AS rs
            WHERE SIZE(rs) > 1
            UNWIND TAIL(rs) AS r
            DELETE r
            """.trimIndent()
        ).run()
    }

    private fun addLatestVersionLabel() {
        neo4jClient.query("""
            MATCH (cpl:Comparison)-[:RELATED]->(cpp:ComparisonPublished)
            WITH cpl, apoc.coll.sortNodes(COLLECT(cpp), 'created_at')[0] AS cpp
            SET cpp:LatestVersion
            RETURN COUNT(cpp) AS count
            """.trimIndent()
        ).run()
    }

    private fun createHeadComparison(version: VersionSubgraph) {
        val statements = (
            statementRepository.fetchAsBundle(
                id = version.latestVersion.id,
                configuration = firstBundleConfig,
                sort = Sort.unsorted()
            ) union statementRepository.fetchAsBundle(
                id = version.latestVersion.id,
                configuration = secondBundleConfig,
                sort = Sort.unsorted()
            )
        ).groupBy { it.subject.id }
        val directStatements = statements[version.latestVersion.id].orEmpty()
        val firstVersion = version.firstVersion
        val lastVersion = version.latestVersion
        val head = Resource(
            id = resourceRepository.nextIdentity(),
            label = lastVersion.label,
            createdAt = firstVersion.createdAt,
            classes = setOf(Classes.comparison),
            createdBy = firstVersion.createdBy,
            observatoryId = firstVersion.observatoryId,
            extractionMethod = lastVersion.extractionMethod,
            organizationId = firstVersion.organizationId,
            visibility = lastVersion.visibility,
            verified = false,
            modifiable = lastVersion.modifiable
        )
        resourceRepository.save(head)

        comparisonPublishedRepository.findById(version.latestVersion.id).ifPresent {
            comparisonTableRepository.save(ComparisonTable(head.id, it.config, it.data))
        }

        directStatements.clone(head, Predicates.description)
        directStatements.clone(head, Predicates.hasSubject)
        directStatements.clone(head, Predicates.reference)
        directStatements.clone(head, Predicates.isAnonymized)
        directStatements.clone(head, Predicates.sustainableDevelopmentGoal)
        directStatements.clone(head, Predicates.comparesContribution)
        directStatements.cloneAuthorList(head, statements)
        directStatements.cloneVisualizations(head, statements)
        directStatements.cloneRelatedFigures(head, statements)
        directStatements.cloneRelatedResources(head, statements)

        version.headVersions.forEach {
            statementService.create(
                userId = head.createdBy,
                subject = head.id,
                predicate = Predicates.hasPublishedVersion,
                `object` = it.id
            )
        }

        if (version.statements.isNotEmpty()) {
            neo4jClient.query("""
                MATCH (c:Comparison {id: ${'$'}id})
                WITH c
                CALL {
                    WITH c
                    UNWIND ${'$'}statement_ids AS statement_id
                    MATCH ()-[r:RELATED {statement_id: statement_id}]->()
                    WITH c, r
                    SET r.predicate_id = "${Predicates.hasPublishedVersion.value}"
                    WITH c, r
                    CALL apoc.refactor.from(r, c)
                    YIELD input, output
                    RETURN input, output
                }
                RETURN COUNT(c)
                """.trimIndent()
            )
                .bindAll(
                    mapOf(
                        "id" to head.id.value,
                        "statement_ids" to version.statements.map { it.value }
                    )
                )
                .run()
        }
    }

    private fun List<GeneralStatement>.cloneAuthorList(newSubject: Resource, statements: Map<ThingId, List<GeneralStatement>>) {
        val authorListStatement = wherePredicate(Predicates.hasAuthors).singleOrNull()
        val authorList = authorListStatement?.`object`
        val authors = authorList?.let { statements[it.id] }.orEmpty()
            .sortedBy { it.index }
            .map {
                if (it.`object` is Literal) (it.`object` as Literal).copy(id = literalRepository.nextIdentity()).also(literalRepository::save).id
                else it.`object`.id
            }
        val newAuthorList = OrkgList(
            id = listRepository.nextIdentity(),
            label = "authors list",
            createdBy = (authorList ?: newSubject).createdBy,
            createdAt = (authorList ?: newSubject).createdAt,
            elements = authors
        )
        listRepository.save(newAuthorList, newAuthorList.createdBy)
        if (authorListStatement != null) {
            statementRepository.save(
                authorListStatement.copy(
                    id = statementRepository.nextIdentity(),
                    subject = newSubject,
                    `object` = resourceRepository.findById(newAuthorList.id).orElseThrow()
                )
            )
        } else {
            statementService.create(
                userId = newAuthorList.createdBy,
                subject = newSubject.id,
                predicate = Predicates.hasAuthors,
                `object` = newAuthorList.id
            )
        }
    }

    private fun List<GeneralStatement>.cloneVisualizations(newSubject: Resource, statements: Map<ThingId, List<GeneralStatement>>) {
        wherePredicate(Predicates.hasVisualization).forEach { hasVisualization ->
            val id = hasVisualization.`object`.id
            val directStatements = statements[id].orEmpty()
            val newVisualization = hasVisualization.cloneWithResourceObject(newSubject)
            directStatements.cloneAuthorList(newVisualization, statements)
            directStatements.clone(newVisualization, Predicates.description)
            visualizationDataRepository.findById(id).ifPresent { visualizationData ->
                visualizationDataRepository.save(
                    visualizationData.copy(
                        id = newVisualization.id,
                        data = (visualizationData.data as ObjectNode).put("orkgOrigin", newVisualization.id.value)
                    )
                )
            }
        }
    }

    private fun List<GeneralStatement>.cloneRelatedFigures(newSubject: Resource, statements: Map<ThingId, List<GeneralStatement>>) {
        wherePredicate(Predicates.hasRelatedFigure).forEach { hasRelatedFigure ->
            val directStatements = statements[hasRelatedFigure.`object`.id].orEmpty()
            val newRelatedFigure = hasRelatedFigure.cloneWithResourceObject(newSubject)
            directStatements.clone(newRelatedFigure, Predicates.hasImage)
            directStatements.clone(newRelatedFigure, Predicates.description)
        }
    }

    private fun List<GeneralStatement>.cloneRelatedResources(newSubject: Resource, statements: Map<ThingId, List<GeneralStatement>>) {
        wherePredicate(Predicates.hasRelatedResource).forEach { hasRelatedResource ->
            val directStatements = statements[hasRelatedResource.`object`.id].orEmpty()
            val newRelatedResource = hasRelatedResource.cloneWithResourceObject(newSubject)
            directStatements.clone(newRelatedResource, Predicates.hasURL)
            directStatements.clone(newRelatedResource, Predicates.hasImage)
            directStatements.clone(newRelatedResource, Predicates.description)
        }
    }

    private fun GeneralStatement.cloneWithResourceObject(newSubject: Resource): Resource {
        val newObject = (`object` as Resource).copy(id = resourceRepository.nextIdentity())
        resourceRepository.save(newObject)
        statementRepository.save(
            copy(
                id = statementRepository.nextIdentity(),
                subject = newSubject,
                `object` = newObject
            )
        )
        return newObject
    }

    private fun List<GeneralStatement>.clone(newSubject: Resource, predicateIdFilter: ThingId) {
        wherePredicate(predicateIdFilter).forEach { statement ->
            val `object` = with(statement.`object`) {
                if (this is Literal) copy(id = literalRepository.nextIdentity()).also(literalRepository::save)
                else this
            }
            statementRepository.save(
                statement.copy(
                    id = statementRepository.nextIdentity(),
                    subject = newSubject,
                    `object` = `object`
                )
            )
        }
    }

    data class VersionSubgraph(val graph: Map<Resource, ObjectRelation?>) {
        val firstVersion: Resource = graph.entries.single { it.value == null }.key
        val latestVersion: Resource = graph.keys.maxBy { it.createdAt }
        val headVersions: List<Resource> = graph.keys.filter { key -> graph.values.none { it?.objectId == key.id } }
        val statements: List<StatementId> = graph.values.filterNotNull().map { it.id }
    }

    data class ObjectRelation(
        val id: StatementId,
        val objectId: ThingId
    )
}
