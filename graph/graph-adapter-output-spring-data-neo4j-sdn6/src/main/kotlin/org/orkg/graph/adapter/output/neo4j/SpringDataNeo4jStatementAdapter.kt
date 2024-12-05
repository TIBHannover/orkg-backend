package org.orkg.graph.adapter.output.neo4j

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Cypher.anonParameter
import org.neo4j.cypherdsl.core.Cypher.anyNode
import org.neo4j.cypherdsl.core.Cypher.asExpression
import org.neo4j.cypherdsl.core.Cypher.call
import org.neo4j.cypherdsl.core.Cypher.collect
import org.neo4j.cypherdsl.core.Cypher.countDistinct
import org.neo4j.cypherdsl.core.Expression
import org.neo4j.cypherdsl.core.Node
import org.neo4j.cypherdsl.core.Cypher.exists
import org.neo4j.cypherdsl.core.Cypher.listOf
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.noCondition
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.parameter
import org.neo4j.cypherdsl.core.Cypher.returning
import org.neo4j.cypherdsl.core.Cypher.union
import org.neo4j.cypherdsl.core.Cypher.toUpper
import org.neo4j.cypherdsl.core.Cypher.unwind
import org.neo4j.cypherdsl.core.Cypher.count
import org.neo4j.cypherdsl.core.Cypher.valueAt
import org.neo4j.cypherdsl.core.ExposesWith
import org.neo4j.cypherdsl.core.Relationship
import org.neo4j.cypherdsl.core.StatementBuilder
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.neo4jdsl.CypherQueryBuilder
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countDistinctOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.countOver
import org.orkg.common.neo4jdsl.PagedQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.QueryCache.Uncached
import org.orkg.common.neo4jdsl.SingleQueryBuilder.fetchAs
import org.orkg.common.neo4jdsl.SingleQueryBuilder.mappedBy
import org.orkg.common.neo4jdsl.sortedWith
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jStatementIdGenerator
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.PredicateUsageCount
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.ResourceContributor
import org.orkg.graph.domain.SearchFilter
import org.orkg.graph.domain.SearchFilter.Operator
import org.orkg.graph.domain.SearchFilter.Value
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.OwnershipInfo
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

private const val RELATED = "RELATED"

@Component
class SpringDataNeo4jStatementAdapter(
    private val neo4jStatementIdGenerator: Neo4jStatementIdGenerator,
    private val predicateRepository: PredicateRepository,
    private val neo4jClient: Neo4jClient,
    private val cacheManager: CacheManager? = null,
) : StatementRepository {

    override fun nextIdentity(): StatementId {
        // IDs could exist already by manual creation. We need to find the next available one.
        var id: StatementId
        do {
            id = neo4jStatementIdGenerator.nextIdentity()
        } while (exists(id))
        return id
    }

    override fun save(statement: GeneralStatement) {
        CypherQueryBuilder(neo4jClient)
            .withQuery {
                val subject = node("Thing")
                    .withProperties("id", parameter("subjectId"))
                    .named("sub")
                val `object` = node("Thing")
                    .withProperties("id", parameter("objectId"))
                    .named("obj")
                match(subject)
                    .match(`object`)
                    .create(
                        subject.relationshipTo(`object`, RELATED).withProperties(
                            "statement_id", parameter("id"),
                            "predicate_id", parameter("predicateId"),
                            "created_by", parameter("createdBy"),
                            "created_at", parameter("createdAt"),
                            "index", parameter("index"),
                            "modifiable", parameter("modifiable")
                        )
                    )
            }
            .withParameters(
                "id" to statement.id.value,
                "subjectId" to statement.subject.id.value,
                "objectId" to statement.`object`.id.value,
                "predicateId" to statement.predicate.id.value,
                "createdBy" to statement.createdBy.value.toString(),
                "createdAt" to statement.createdAt!!.format(ISO_OFFSET_DATE_TIME),
                "index" to statement.index,
                "modifiable" to statement.modifiable
            )
            .run()
    }

    override fun saveAll(statements: Set<GeneralStatement>) {
        CypherQueryBuilder(neo4jClient)
            .withQuery {
                val statement = name("statement")
                val subject = node("Thing").named("s")
                val `object` = node("Thing").named("o")
                unwind(parameter("data"))
                    .`as`(statement)
                    .with(statement)
                    .match(subject.withProperties("id", valueAt(statement, 0)))
                    .match(`object`.withProperties("id", valueAt(statement, 1)))
                    .create(
                        subject.relationshipTo(`object`, RELATED).withProperties(
                            "statement_id", valueAt(statement, 2),
                            "predicate_id", valueAt(statement, 3),
                            "created_by", valueAt(statement, 4),
                            "created_at", valueAt(statement, 5),
                            "index", valueAt(statement, 6),
                            "modifiable", valueAt(statement, 7)
                        )
                    )
            }
            .withParameters(
                "data" to statements.map {
                    listOf(
                        it.subject.id.value,
                        it.`object`.id.value,
                        it.id.value,
                        it.predicate.id.value,
                        it.createdBy.value.toString(),
                        it.createdAt?.format(ISO_OFFSET_DATE_TIME),
                        it.index,
                        it.modifiable
                    )
                }
            )
            .run()
    }

    override fun count(): Long = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val r = name("rel")
            match(anyNode().relationshipTo(anyNode(), RELATED).named(r))
                .returning(count(r))
        }
        .fetchAs<Long>()
        .one()
        .orElse(0)

    override fun delete(statement: GeneralStatement) = deleteByStatementId(statement.id)

    override fun deleteByStatementId(id: StatementId) {
        CypherQueryBuilder(neo4jClient)
            .withQuery {
                val o = name("obj")
                val l = name("l")
                val node = anyNode().named(o)
                val relation = node("Thing")
                    .relationshipTo(node, RELATED)
                    .withProperties("statement_id", parameter("id"))
                match(relation)
                    .delete(relation)
                    .with(o)
                    .where(
                        node.hasLabels("Literal")
                            .and(node.relationshipBetween(anyNode()).asCondition().not())
                            .and(o.property("modifiable").eq(literalOf<Boolean>(true)))
                    ).with(o.property("id").`as`(l), o.`as`(o))
                    .delete(o)
                    .returning(l)
            }
            .withParameters("id" to id.value)
            .fetchAs(ThingId::class)
            .mappedBy { _, r -> r["l"].toThingId()!! }
            .one()
            .ifPresent(::evictFromCaches)
    }

    override fun deleteByStatementIds(ids: Set<StatementId>) {
        val literals = CypherQueryBuilder(neo4jClient)
            .withQuery {
                val o = name("obj")
                val l = name("l")
                val id = name("id")
                val node = anyNode().named(o)
                val relation = node("Thing")
                    .relationshipTo(node, RELATED)
                    .withProperties("statement_id", id)
                unwind(parameter("ids"))
                    .`as`(id)
                    .with(id)
                    .match(relation)
                    .delete(relation)
                    .with(o)
                    .where(
                        node.hasLabels("Literal")
                            .and(node.relationshipBetween(anyNode()).asCondition().not())
                    ).with(o.property("id").`as`(l), o.`as`(o))
                    .delete(o)
                    .returning(l)
            }
            .withParameters("ids" to ids.map { it.value })
            .mappedBy { _, r -> r["l"].toThingId()!! }
            .all()

        if (literals.isNotEmpty()) {
            literals.forEach(::evictFromCaches)
        }
    }

    override fun deleteAll() {
        CypherQueryBuilder(neo4jClient)
            .withQuery {
                val r = name("rel")
                match(anyNode().relationshipTo(anyNode(), RELATED).named(r))
                    .delete(r)
            }
            .run()
    }

    override fun findAll(pageable: Pageable): Page<GeneralStatement> =
        findAll(
            pageable = pageable,
            subjectClasses = emptySet(),
            subjectId = null,
            subjectLabel = null,
            predicateId = null,
            createdBy = null,
            createdAtStart = null,
            createdAtEnd = null,
            objectClasses = emptySet(),
            objectId = null,
            objectLabel = null
        )

    override fun countIncomingStatements(id: ThingId): Long = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val r = name("rel")
            val subject = node("Thing")
            val `object` = node("Thing")
                .withProperties("id", parameter("id"))
            match(
                subject.relationshipTo(`object`, RELATED)
                    .named(r)
            ).returning(count(r))
        }
        .withParameters("id" to id.value)
        .fetchAs<Long>()
        .one()
        .orElse(0)

    override fun countIncomingStatements(ids: Set<ThingId>): Map<ThingId, Long> =
        CypherQueryBuilder(neo4jClient)
            .withQuery {
                val r = name("rel")
                val id = name("id")
                val `object` = node("Thing").withProperties("id", id)
                val count = name("count")
                unwind(parameter("ids")).`as`(id)
                    .match(
                        node("Thing").relationshipTo(`object`, RELATED)
                            .named(r)
                    )
                    .with(id, count(r).`as`(count))
                    .returning(id, count)
            }
            .withParameters("ids" to ids.map { it.value })
            .mappedBy { _, record -> ThingId(record["id"].asString()) to record["count"].asLong() }
            .all()
            .toMap()

    override fun findAllDescriptions(ids: Set<ThingId>): Map<ThingId, String> =
        CypherQueryBuilder(neo4jClient)
            .withQuery {
                val id = name("id")
                val subject = node("Thing").withProperties("id", id)
                val `object` = node("Literal")
                unwind(parameter("ids")).`as`(id)
                    .match(
                        subject.relationshipTo(`object`, RELATED)
                            .withProperties("predicate_id", literalOf<String>(Predicates.description.value))
                    )
                    .returning(id, valueAt(collect(`object`.property("label")), 0).`as`("description"))
            }
            .withParameters("ids" to ids.map { it.value })
            .mappedBy { _, record -> ThingId(record["id"].asString()) to record["description"].asString() }
            .all()
            .toMap()

    override fun determineOwnership(statementIds: Set<StatementId>): Set<OwnershipInfo> =
        CypherQueryBuilder(neo4jClient)
            .withQuery {
                val id = name("id")
                val statementId = name("statementId")
                val owner = name("owner")
                val r = name("rel")
                val subject = node("Thing")
                val `object` = node("Thing")
                unwind(parameter("statementIds"))
                    .`as`(id)
                    .with(id)
                    .match(
                        subject.relationshipTo(`object`, RELATED)
                            .withProperties("statement_id", id)
                            .named(r)
                    )
                    .returning(r.property("statement_id").`as`(statementId), r.property("created_by").`as`(owner))
            }
            .withParameters("statementIds" to statementIds.map(StatementId::value))
            .mappedBy { _, record ->
                OwnershipInfo(record["statementId"].toStatementId(), record["owner"].toContributorId())
            }
            .all()
            .toSet()

    override fun findByStatementId(id: StatementId): Optional<GeneralStatement> = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val r = name("rel")
            val subject = node("Thing")
            val `object` = node("Thing")
            match(
                subject.relationshipTo(`object`, RELATED).withProperties(
                    "statement_id", parameter("id")
                ).named(r)
            ).returning(r, subject.`as`("sub"), `object`.`as`("obj"))
        }
        .withParameters("id" to id.value)
        .mappedBy(StatementMapper(predicateRepository))
        .one()

    override fun findAll(
        pageable: Pageable,
        subjectClasses: Set<ThingId>,
        subjectId: ThingId?,
        subjectLabel: String?,
        predicateId: ThingId?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        objectClasses: Set<ThingId>,
        objectId: ThingId?,
        objectLabel: String?
    ): Page<GeneralStatement> = CypherQueryBuilder(neo4jClient, Uncached)
        .withCommonQuery {
            val subject = node("Thing").named("sub")
            val r = name("r")
            val `object` = node("Thing").named("obj")
            match(subject.relationshipTo(`object`, RELATED).named(r))
                .with(r, subject, `object`)
                .where(
                    subjectClasses.toCondition { subject.hasLabels(*it.map(ThingId::value).toTypedArray()) },
                    subjectId.toCondition { subject.property("id").eq(anonParameter(it.value)) },
                    subjectLabel.toCondition { subject.property("label").eq(anonParameter(it)) },
                    predicateId.toCondition { r.property("predicate_id").eq(anonParameter(it.value)) },
                    createdBy.toCondition { r.property("created_by").eq(anonParameter(it.value.toString())) },
                    createdAtStart.toCondition { r.property("created_at").gte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                    createdAtEnd.toCondition { r.property("created_at").lte(anonParameter(it.format(ISO_OFFSET_DATE_TIME))) },
                    objectClasses.toCondition { `object`.hasLabels(*it.map(ThingId::value).toTypedArray()) },
                    objectId.toCondition { `object`.property("id").eq(anonParameter(it.value)) },
                    objectLabel.toCondition { `object`.property("label").eq(anonParameter(it)) }
                )
        }
        .withQuery { commonQuery ->
            val subject = name("sub")
            val r = name("r")
            val `object` = name("obj")
            val sort = pageable.sort.orElseGet { Sort.by("created_at") }
            val propertyMappings = mapOf(
                "id" to r.property("id"),
                "created_at" to r.property("created_at"),
                "created_by" to r.property("created_by"),
                "index" to r.property("index"),
                "sub.id" to subject.property("id"),
                "sub.label" to subject.property("label"),
                "sub.created_at" to subject.property("created_at"),
                "sub.created_by" to subject.property("created_by"),
                "obj.id" to `object`.property("id"),
                "obj.label" to `object`.property("label"),
                "obj.created_at" to `object`.property("created_at"),
                "obj.created_by" to `object`.property("created_by"),
            )
            commonQuery
                .with(r, subject, `object`)
                .where(
                    orderByOptimizations(
                        propertyMappings = propertyMappings,
                        sort = sort,
                        properties = arrayOf("id", "created_at", "created_by", "index")
                    )
                )
                .with(r, subject, `object`)
                .orderBy(
                    sort.toSortItems(
                        propertyMappings = propertyMappings,
                        knownProperties = arrayOf(
                            "id", "created_at", "created_by", "index",
                            "sub.id", "sub.label", "sub.created_at", "sub.created_by",
                            "obj.id", "obj.label", "obj.created_at", "obj.created_by"
                        )
                    )
                )
                .returning(r.`as`("rel"), subject, `object`)
        }
        .countOver("r")
        .mappedBy(StatementMapper(predicateRepository))
        .fetch(pageable, false)

    override fun findAllByStatementIdIn(ids: Set<StatementId>, pageable: Pageable): Page<GeneralStatement> =
        CypherQueryBuilder(neo4jClient)
            .withCommonQuery {
                val subject = node("Thing").named("sub")
                val `object` = node("Thing").named("obj")
                val id = name("id")
                unwind(parameter("ids"))
                    .`as`(id)
                    .with(id)
                    .match(
                        subject.relationshipTo(`object`, RELATED)
                            .withProperties("statement_id", id)
                            .named("r")
                    )
            }
            .withQuery { commonQuery ->
                commonQuery.returningWithSortableFields("r", "sub", "obj")
            }
            .countOver("r")
            .withParameters("ids" to ids.map { it.value })
            .mappedBy(StatementMapper(predicateRepository))
            .fetch(pageable)

    override fun countByIdRecursive(id: ThingId): Long = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val apocConfiguration = mapOf<String, Any>(
                "relationshipFilter" to ">",
                "labelFilter" to "-ResearchField|-Problem|-Paper"
            )
            val n = name("n")
            val relationships = name("relationships")
            val rel = name("rel")

            match(
                node("Thing")
                    .withProperties("id", parameter("id"))
                    .named(n)
            )
                .call("custom.subgraph")
                .withArgs(n, asExpression(apocConfiguration))
                .yield(relationships)
                .with(relationships)
                .unwind(relationships).`as`(rel)
                .returning(count(rel))
        }
        .withParameters("id" to id.value)
        .fetchAs<Long>()
        .one()
        .orElse(0)

    override fun findAllBySubjects(subjectIds: List<ThingId>, pageable: Pageable): Page<GeneralStatement> =
        findAllFilteredAndPaged(
            parameters = mapOf("subjectIds" to subjectIds.map { it.value }),
            pageable = pageable
        ) { subject, _, _ ->
            subject.property("id").`in`(parameter("subjectIds"))
        }

    override fun findAllByObjects(objectIds: List<ThingId>, pageable: Pageable): Page<GeneralStatement> =
        findAllFilteredAndPaged(
            parameters = mapOf("objectIds" to objectIds.map { it.value }),
            pageable = pageable
        ) { _, _, `object` ->
            `object`.property("id").`in`(parameter("objectIds"))
        }

    override fun fetchAsBundle(id: ThingId, configuration: BundleConfiguration, sort: Sort): Iterable<GeneralStatement> =
        CypherQueryBuilder(neo4jClient)
            .withQuery {
                val n = name("n")
                val relationships = name("relationships")
                val rel = name("rel")
                val node = node("Thing")
                    .withProperties("id", parameter("id"))
                    .named(n)
                match(node)
                    .call("custom.subgraph")
                    .withArgs(n, parameter("config"))
                    .yield(relationships)
                    .with(relationships)
                    .unwind(relationships).`as`(rel)
                    .with(startNode(rel).`as`("sub"), rel.`as`("rel"), endNode(rel).`as`("obj"))
                    .orderBy(rel.property("created_at").descending())
                    .returningWithSortableFields("rel", "sub", "obj")
                    .orderBy(sort.toSortItems())
            }
            .withParameters(
                "id" to id.value,
                "config" to configuration.toApocConfiguration()
            )
            .mappedBy(StatementMapper(predicateRepository))
            .all()

    override fun exists(id: StatementId): Boolean = CypherQueryBuilder(neo4jClient)
        .withQuery {
            returning(
                exists(
                    anyNode().relationshipTo(anyNode(), RELATED)
                        .withProperties("statement_id", parameter("id"))
                )
            )
        }
        .withParameters("id" to id.value)
        .fetchAs<Boolean>()
        .one()
        .orElse(false)

    override fun countPredicateUsage(pageable: Pageable): Page<PredicateUsageCount> = CypherQueryBuilder(neo4jClient)
        .withCommonQuery {
            match(anyNode().relationshipTo(anyNode(), RELATED).named("rel"))
        }
        .withQuery { commonQuery ->
            val r = name("rel")
            val c = name("c")
            val id = name("id")
            commonQuery.with(r.property("predicate_id").`as`(id), count(r).`as`(c))
                .orderBy(c.descending(), id.ascending())
                .returning(id, c)
        }
        .withCountQuery { commonQuery ->
            commonQuery.returning(countDistinct(name("rel").property("predicate_id")))
        }
        .mappedBy { _, record -> PredicateUsageCount(ThingId(record["id"].asString()), record["c"].asLong()) }
        .fetch(pageable)

    override fun findDOIByContributionId(id: ThingId): Optional<Literal> = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val doi = name("doi")
            val relations = node("Resource")
                .withProperties("id", parameter("id"))
                .relationshipFrom(paperNode(), RELATED)
                .withProperties("predicate_id", literalOf<String>(Predicates.hasContribution.value))
                .relationshipTo(node("Literal").named(doi), RELATED)
                .properties("predicate_id", literalOf<String>(Predicates.hasDOI.value))
            match(relations).returning(doi)
        }
        .withParameters("id" to id.value)
        .mappedBy(LiteralMapper("doi"))
        .one()

    override fun findByDOI(doi: String, classes: Set<ThingId>): Optional<Resource> = CypherQueryBuilder(neo4jClient, Uncached)
        .withQuery {
            val p = node("Resource").named("p")
            val l = name("l")
            match(
                p.relationshipTo(node("Literal").named(l), RELATED)
                    .withProperties("predicate_id", literalOf<String>(Predicates.hasDOI.value))
            ).where(
                toUpper(l.property("label")).eq(toUpper(parameter("doi")))
                    .and(classes.map { p.hasLabels(it.value) }.reduceOrNull(Condition::or) ?: noCondition())
            ).returning(p)
                .orderBy(p.property("created_at").descending())
                .limit(1)
        }
        .withParameters("doi" to doi)
        .mappedBy(ResourceMapper("p"))
        .one()

    override fun findAllBySubjectClassAndDOI(subjectClass: ThingId, doi: String, pageable: Pageable): Page<Resource> =
        CypherQueryBuilder(neo4jClient)
            .withCommonQuery {
                val resource = node("Resource")
                    .named("n")
                val l = name("l")
                match(
                    resource.relationshipTo(node("Literal").named(l), RELATED)
                        .withProperties("predicate_id", literalOf<String>(Predicates.hasDOI.value))
                ).where(
                    resource.hasLabels(subjectClass.value)
                        .and(toUpper(l.property("label")).eq(toUpper(parameter("doi"))))
                )
            }.withQuery { commonQuery ->
                commonQuery.returningDistinct("n")
            }
            .countDistinctOver("n")
            .withParameters("doi" to doi)
            .mappedBy(ResourceMapper("n"))
            .fetch(pageable)

    override fun findAllProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource> =
        CypherQueryBuilder(neo4jClient)
            .withCommonQuery {
                val problem = name("p")
                val idParameter = parameter("id")
                call(
                    union(
                        match(
                            paperNode()
                                .withProperties("organization_id", idParameter)
                                .relationshipTo(contributionNode(), RELATED)
                                .withProperties("predicate_id", literalOf<String>("P31"))
                                .relationshipTo(problemNode().named(problem), RELATED)
                                .properties("predicate_id", literalOf<String>("P32"))
                        ).returning(problem)
                            .build(),
                        match(
                            node("Problem")
                                .named(problem)
                                .withProperties("observatory_id", idParameter)
                        ).returning(problem)
                            .build()
                    )
                )
            }
            .withQuery { commonQuery ->
                val problem = name("p")
                commonQuery.with(problem)
                    .orderBy(problem.property("id").ascending())
                    .returning(problem)
            }
            .countOver("p")
            .withParameters("id" to id.value.toString())
            .mappedBy(ResourceMapper("p"))
            .fetch(pageable)

    override fun findAllContributorsByResourceId(id: ThingId, pageable: Pageable): Page<ContributorId> =
        CypherQueryBuilder(neo4jClient)
            .withCommonQuery {
                val apocConfiguration = mapOf<String, Any>(
                    "relationshipFilter" to ">",
                    "labelFilter" to "-ResearchField|-Problem|-Paper"
                )
                val n = name("n")
                val relationships = name("relationships")
                val rel = name("rel")
                val nodes = name("nodes")
                val node = name("node")
                val createdBy = name("createdBy")
                match(
                    node("Resource")
                        .withProperties("id", parameter("id"))
                        .named(n)
                ).call("custom.subgraph")
                    .withArgs(n, asExpression(apocConfiguration))
                    .yield(relationships)
                    .with(relationships, n)
                    .unwind(relationships).`as`(rel)
                    .with(collect(rel).`as`(rel), collect(endNode(rel)).`as`(nodes), n.`as`(n))
                    .withDistinct(rel.add(nodes).add(n).`as`(nodes))
                    .unwind(nodes).`as`(node)
                    .withDistinct(node.property("created_by").`as`(createdBy))
                    .where(createdBy.isNotNull)
            }
            .withQuery { commonQuery ->
                val createdBy = name("createdBy")
                commonQuery.with(createdBy)
                    .orderBy(createdBy.ascending())
                    .returning(createdBy)
            }
            .countOver("createdBy")
            .withParameters("id" to id.value)
            .mappedBy { _, record -> record["createdBy"].toContributorId() }
            .fetch(pageable)

    override fun findTimelineByResourceId(id: ThingId, pageable: Pageable): Page<ResourceContributor> =
        CypherQueryBuilder(neo4jClient)
            .withCommonQuery {
                val apocConfiguration = mapOf<String, Any>(
                    "relationshipFilter" to ">",
                    "labelFilter" to "-ResearchField|-Problem|-Paper"
                )
                val n = name("n")
                val relationships = name("relationships")
                val rel = name("rel")
                val nodes = name("nodes")
                val node = name("node")
                val createdBy = name("createdBy")
                val ms = name("ms")
                val edit = listOf(
                    createdBy,
                    call("apoc.date.format")
                        .withArgs(
                            ms,
                            literalOf<String>("ms"),
                            literalOf<String>("yyyy-MM-dd'T'HH:mm:'00'XXX")
                        )
                        .asFunction()
                ).`as`("edit")

                match(
                    node("Resource")
                        .withProperties("id", parameter("id"))
                        .named(n)
                ).call("custom.subgraph")
                    .withArgs(n, asExpression(apocConfiguration))
                    .yield(relationships)
                    .with(relationships, n)
                    .unwind(relationships).`as`(rel)
                    .withDistinct(collect(rel).add(collect(endNode(rel))).add(collect(n)).`as`(nodes), n)
                    .unwind(nodes).`as`(node)
                    .with(node, n)
                    .where(
                        node.property("created_by").isNotNull
                            .and(node.property("created_at").isNotNull)
                            .and(node.property("created_at").gte(n.property("created_at")))
                    )
                    .with(
                        node.property("created_by").`as`(createdBy),
                        call("custom.parseIsoOffsetDateTime")
                            .withArgs(node.property("created_at"), literalOf<String>("ms"))
                            .asFunction().`as`(ms)
                    )
                    .withDistinct(edit)
            }
            .withQuery { commonQuery ->
                val edit = name("edit")
                val createdBy = name("createdBy")
                val createdAt = name("createdAt")
                commonQuery.with(valueAt(edit, 0).`as`(createdBy), valueAt(edit, 1).`as`(createdAt))
                    .orderBy(createdAt.descending())
                    .returning(createdBy, createdAt)
            }
            .countOver("edit")
            .withParameters("id" to id.value)
            .mappedBy { _, record -> ResourceContributor(record["createdBy"].toContributorId(), record["createdAt"].toOffsetDateTime()) }
            .fetch(pageable)

    override fun findAllProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource> =
        CypherQueryBuilder(neo4jClient)
            .withCommonQuery {
                match(
                    comparisonNode()
                        .withProperties("organization_id", parameter("id"))
                        .relationshipTo(contributionNode(), RELATED)
                        .withProperties("predicate_id", literalOf<String>("compareContribution"))
                        .relationshipTo(problemNode().named("p"), RELATED)
                        .properties("predicate_id", literalOf<String>("P32"))
                )
            }.withQuery { commonQuery ->
                commonQuery.returningDistinct(name("p"))
            }
            .countDistinctOver("p")
            .withParameters("id" to id.value.toString())
            .mappedBy(ResourceMapper("p"))
            .fetch(pageable)

    override fun findAllPapersByObservatoryIdAndFilters(
        observatoryId: ObservatoryId?,
        filters: List<SearchFilter>,
        visibility: VisibilityFilter,
        pageable: Pageable
    ): Page<Resource> {
        val matchPaper = buildString {
            append("MATCH (paper:Paper:Resource")
            if (observatoryId != null) {
                append(" {observatory_id: ${'$'}observatoryId}")
            }
            append(")")
        }
        val matchFilters = if (filters.isEmpty()) "" else {
            filters.mapIndexed { filterIndex, filter ->
                filter.path.joinToString(
                    prefix = buildString {
                        append("""MATCH (ctr)""")
                        if (!filter.exact) {
                            append("""-[:RELATED*0..""")
                            append(0.coerceAtLeast(10 - filter.path.size))
                            append("""]->(:Thing)""")
                        }
                    },
                    postfix = when (filter.range) {
                        Classes.string -> """(n$filterIndex:Literal {datatype: "xsd:string"})"""
                        Classes.integer -> """(n$filterIndex:Literal {datatype: "xsd:integer"})"""
                        Classes.decimal -> """(n$filterIndex:Literal {datatype: "xsd:decimal"})"""
                        Classes.boolean -> """(n$filterIndex:Literal {datatype: "xsd:boolean"})"""
                        Classes.uri -> """(n$filterIndex:Literal {datatype: "xsd:anyURI"})"""
                        Classes.date -> """(n$filterIndex:Literal {datatype: "xsd:date"})"""
                        Classes.classes -> """(n$filterIndex:Class)"""
                        Classes.predicates -> """(n$filterIndex:Predicate)"""
                        else -> """(n$filterIndex:Resource)"""
                    },
                    separator = "(:Thing)"
                ) { """-[:RELATED {predicate_id: "$it"}]->""" }
            }.joinToString(
                separator = " ",
                // The contribution needs to be matched only when filters exist,
                // otherwise only papers that have contributions would be returned
                prefix = """MATCH (paper)-[:RELATED {predicate_id: "P31"}]->(ctr:Contribution) """
            )
        }
        val filterValuesWithAlias = if (filters.isEmpty()) "" else {
            filters.withIndex().joinToString(prefix = ", ") { (filterIndex, filter) ->
                when (filter.range) {
                    Classes.string, Classes.integer, Classes.decimal, Classes.boolean, Classes.uri, Classes.date -> "n$filterIndex.label"
                    else -> "n$filterIndex.id"
                } + " AS value$filterIndex"
            }
        }
        val withPaperAndValues = """WITH paper$filterValuesWithAlias"""
        val andFilterValuesMatch = if (filters.isEmpty()) "" else {
            filters.mapIndexed { filterIndex, filter ->
                filter.values.withIndex().joinToString(
                    separator = " OR ",
                    prefix = "(",
                    postfix = ")"
                ) { (valueIndex, value) ->
                    val op = when (value.op) {
                        Operator.EQ -> "="
                        Operator.NE -> "<>"
                        Operator.LT -> "<"
                        Operator.GT -> ">"
                        Operator.LE -> "<="
                        Operator.GE -> ">="
                    }
                    "value$filterIndex $op ${'$'}values[$filterIndex][$valueIndex]"
                }
            }.joinToString(prefix = " AND (", separator = " AND ", postfix = ")")
        }
        val whereVisibility = when (visibility) {
            VisibilityFilter.ALL_LISTED -> """WHERE (paper.visibility = "DEFAULT" OR paper.visibility = "FEATURED")"""
            VisibilityFilter.UNLISTED -> """WHERE paper.visibility = "UNLISTED""""
            VisibilityFilter.FEATURED -> """WHERE paper.visibility = "FEATURED""""
            VisibilityFilter.NON_FEATURED -> """WHERE paper.visibility = "DEFAULT""""
            VisibilityFilter.DELETED -> """WHERE paper.visibility = "DELETED""""
        }
        val whereVisibilityAndValues = whereVisibility + andFilterValuesMatch
        val filterValues = if (filters.isEmpty()) "" else {
            filters.indices.joinToString(prefix = ", ") { filterIndex -> "value$filterIndex" }
        }
        val withNodePropertiesAndValues = """WITH paper, paper.id AS id, paper.created_at AS created_at, paper.created_by AS created_by$filterValues"""
        val sort = pageable.sort.orElseGet { Sort.by(Sort.Direction.DESC, "created_at") }
        val commonQuery = "$matchPaper $matchFilters $withPaperAndValues $whereVisibilityAndValues $withNodePropertiesAndValues"
        val query = "$commonQuery RETURN DISTINCT paper SKIP ${'$'}sdnSkip LIMIT ${'$'}sdnLimit".let {
            it.sortedWith(sort, it.lastIndexOf("RETURN"))
        }
        val countQuery = "$commonQuery RETURN COUNT(DISTINCT paper)"
        val parameters = mapOf(
            "observatoryId" to observatoryId?.value?.toString(),
            "values" to filters.map { it.values.map(Value::value) },
            "sdnSkip" to pageable.offset,
            "sdnLimit" to pageable.pageSize
        )
        val elements = neo4jClient.query(query)
            .bindAll(parameters)
            .fetchAs(Resource::class.java)
            .mappedBy(ResourceMapper("paper"))
            .all()
        val count = neo4jClient.query(countQuery)
            .bindAll(parameters)
            .fetchAs(Long::class.java)
            .one()
            .orElse(0)
        return PageImpl(elements.toList(), pageable, count)
    }

    private fun findAllFilteredAndPaged(
        parameters: Map<String, Any>,
        pageable: Pageable,
        subject: Node = node("Thing"),
        `object`: Node = node("Thing"),
        filter: (subject: Node, relationship: Relationship, `object`: Node) -> Condition
    ): Page<GeneralStatement> = CypherQueryBuilder(neo4jClient)
        .withCommonQuery {
            val r = name("rel")
            val relation = subject.relationshipTo(`object`, RELATED)
                .named(r)
            val condition = filter(subject, relation, `object`)
            match(relation).where(condition)
        }
        .withQuery { commonQuery ->
            commonQuery.returningWithSortableFields(name("rel"), subject.asExpression(), `object`.asExpression())
        }
        .countOver("rel")
        .withParameters(parameters)
        .mappedBy(StatementMapper(predicateRepository))
        .fetch(pageable)

    private fun BundleConfiguration.toApocConfiguration(): Map<String, Any> {
        val conf = mutableMapOf<String, Any>(
            "relationshipFilter" to "RELATED>",
            "bfs" to true
        )
        // configure min and max levels
        if (maxLevel != null)
            conf["maxLevel"] = maxLevel!!
        if (minLevel != null)
            conf["minLevel"] = minLevel!!
        // configure blacklisting and whitelisting classes
        var labelFilter = ""
        if (blacklist.isNotEmpty())
            labelFilter = blacklist.joinToString(prefix = "-", separator = "|-")
        if (whitelist.isNotEmpty()) {
            var positiveLabels = whitelist.joinToString(prefix = "+", separator = "|+")
            if (labelFilter.isNotBlank())
                positiveLabels += "|$labelFilter"
            labelFilter = positiveLabels
        }
        if (blacklist.isNotEmpty() || whitelist.isNotEmpty())
            conf["labelFilter"] = labelFilter
        return conf
    }

    private fun evictFromCaches(id: ThingId) {
        cacheManager?.getCache(LITERAL_ID_TO_LITERAL_CACHE)?.evictIfPresent(id)
        cacheManager?.getCache(LITERAL_ID_TO_LITERAL_EXISTS_CACHE)?.evictIfPresent(id)
        cacheManager?.getCache(THING_ID_TO_THING_CACHE)?.evictIfPresent(id)
    }

    private fun ExposesWith.returningWithSortableFields(
        relation: String,
        subject: String,
        `object`: String
    ): StatementBuilder.OngoingReadingAndReturn =
        returningWithSortableFields(name(relation), name(subject), name(`object`))

    private fun ExposesWith.returningWithSortableFields(
        relation: Expression,
        subject: Expression,
        `object`: Expression
    ): StatementBuilder.OngoingReadingAndReturn {
        val sub = name("sub")
        val obj = name("obj")
        val rel = name("rel")
        return with(
            relation.`as`(rel),
            subject.`as`(sub),
            `object`.`as`(obj),
            relation.property("created_at").`as`("created_at"),
            relation.property("created_by").`as`("created_by"),
            relation.property("index").`as`("index")
        ).returning(rel, sub, obj)
    }
}
