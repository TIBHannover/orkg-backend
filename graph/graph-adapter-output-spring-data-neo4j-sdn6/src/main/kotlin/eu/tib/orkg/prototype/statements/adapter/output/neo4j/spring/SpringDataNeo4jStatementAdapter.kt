package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.dsl.CypherQueryBuilder
import eu.tib.orkg.prototype.dsl.PagedQueryBuilder.countDistinctOver
import eu.tib.orkg.prototype.dsl.PagedQueryBuilder.countOver
import eu.tib.orkg.prototype.dsl.PagedQueryBuilder.fetchAs
import eu.tib.orkg.prototype.dsl.PagedQueryBuilder.mappedBy
import eu.tib.orkg.prototype.dsl.SingleQueryBuilder.fetchAs
import eu.tib.orkg.prototype.dsl.SingleQueryBuilder.mappedBy
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatementIdGenerator
import eu.tib.orkg.prototype.statements.api.BundleConfiguration
import eu.tib.orkg.prototype.statements.api.RetrieveStatementUseCase.PredicateUsageCount
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.ObjectService
import eu.tib.orkg.prototype.statements.spi.OwnershipInfo
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceContributor
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Cypher.anyNode
import org.neo4j.cypherdsl.core.Cypher.asExpression
import org.neo4j.cypherdsl.core.Cypher.call
import org.neo4j.cypherdsl.core.Cypher.listOf
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.optionalMatch
import org.neo4j.cypherdsl.core.Cypher.parameter
import org.neo4j.cypherdsl.core.Cypher.returning
import org.neo4j.cypherdsl.core.Cypher.union
import org.neo4j.cypherdsl.core.Cypher.unionAll
import org.neo4j.cypherdsl.core.Cypher.unwind
import org.neo4j.cypherdsl.core.Cypher.valueAt
import org.neo4j.cypherdsl.core.Expression
import org.neo4j.cypherdsl.core.Functions.collect
import org.neo4j.cypherdsl.core.Functions.count
import org.neo4j.cypherdsl.core.Functions.countDistinct
import org.neo4j.cypherdsl.core.Functions.labels
import org.neo4j.cypherdsl.core.Functions.sum
import org.neo4j.cypherdsl.core.Node
import org.neo4j.cypherdsl.core.Predicates.exists
import org.neo4j.cypherdsl.core.Relationship
import org.neo4j.cypherdsl.core.StatementBuilder
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

private const val RELATED = "RELATED"

@Component
class SpringDataNeo4jStatementAdapter(
    private val neo4jStatementIdGenerator: Neo4jStatementIdGenerator,
    private val predicateRepository: PredicateRepository,
    override val neo4jClient: Neo4jClient,
    private val cacheManager: CacheManager? = null,
) : SpringDataNeo4jAdapter(neo4jClient), StatementRepository {

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
                            "index", parameter("index")
                        )
                    )
            }
            .withParameters(
                "id" to statement.id!!.value,
                "subjectId" to statement.subject.id.value,
                "objectId" to statement.`object`.id.value,
                "predicateId" to statement.predicate.id.value,
                "createdBy" to statement.createdBy.value.toString(),
                "createdAt" to statement.createdAt!!.format(ISO_OFFSET_DATE_TIME),
                "index" to statement.index
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
                            "index", valueAt(statement, 6)
                        )
                    )
            }
            .withParameters(
                "data" to statements.map {
                    listOf(
                        it.subject.id.value,
                        it.`object`.id.value,
                        it.id?.value,
                        it.predicate.id.value,
                        it.createdBy.value.toString(),
                        it.createdAt?.format(ISO_OFFSET_DATE_TIME),
                        it.index
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

    override fun delete(statement: GeneralStatement) = deleteByStatementId(statement.id!!)

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
                        literalOf<String>("Literal").`in`(labels(node))
                            .and(node.relationshipBetween(anyNode()).asCondition().not())
                    ).with(o.property("id").`as`(l), o.`as`(o))
                    .delete(o)
                    .returning(l)
            }
            .withParameters("id" to id.value)
            .fetchAs<ThingId>()
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
                        literalOf<String>("Literal").`in`(labels(node))
                            .and(node.relationshipBetween(anyNode()).asCondition().not())
                    ).with(o.property("id").`as`(l), o.`as`(o))
                    .delete(o)
                    .returning(l)
            }
            .withParameters("ids" to ids.map { it.value })
            .fetchAs<ThingId>()
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

    override fun findAll(pageable: Pageable): Page<GeneralStatement> = CypherQueryBuilder(neo4jClient)
        .withCommonQuery {
            val subject = anyNode().named("sub")
            val `object` = anyNode().named("obj")
            match(subject.relationshipTo(`object`, RELATED).named("rel"))
        }
        .withQuery { commonQuery ->
            commonQuery.returningWithSortableFields("rel", "sub", "obj")
        }
        .countOver("rel")
        .fetchAs<GeneralStatement>()
        .mappedBy(StatementMapper(predicateRepository))
        .fetch(pageable)

    override fun countStatementsAboutResource(id: ThingId): Long = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val r = name("rel")
            val subject = node("Thing")
            val `object` = node("Resource")
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

    override fun countStatementsAboutResources(resourceIds: Set<ThingId>): Map<ThingId, Long> =
        CypherQueryBuilder(neo4jClient)
            .withQuery {
                val r = name("rel")
                val subject = node("Thing")
                val `object` = node("Resource")
                val resourceId = `object`.property("id")
                val id = name("id")
                val count = name("count")
                match(
                    subject.relationshipTo(`object`, RELATED)
                        .named(r)
                ).where(resourceId.`in`(parameter("resourceIds")))
                    .with(resourceId.`as`(id), count(r).`as`(count))
                    .returning(id, count)
            }
            .withParameters("resourceIds" to resourceIds.map { it.value })
            .mappedBy { _, record -> ThingId(record["id"].asString()) to record["count"].asLong() }
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

    override fun findAllBySubject(subjectId: ThingId, pageable: Pageable): Page<GeneralStatement> =
        findAllFilteredAndPaged(mapOf("subjectId" to subjectId.value), pageable) { subject, _, _ ->
            subject.property("id").eq(parameter("subjectId"))
        }

    override fun findAllByPredicateId(predicateId: ThingId, pageable: Pageable): Page<GeneralStatement> =
        findAllFilteredAndPaged(mapOf("predicateId" to predicateId.value), pageable) { _, relation, _ ->
            relation.property("predicate_id").eq(parameter("predicateId"))
        }

    override fun findAllByObject(objectId: ThingId, pageable: Pageable): Page<GeneralStatement> =
        findAllFilteredAndPaged(mapOf("objectId" to objectId.value), pageable) { _, _, `object` ->
            `object`.property("id").eq(parameter("objectId"))
        }

    override fun countByIdRecursive(id: ThingId): Long = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val apocConfiguration = mapOf<String, Any>(
                "relationshipFilter" to ">",
                "labelFilter" to "-ResearchField|-ResearchProblem|-Paper"
            )
            val n = name("n")
            val relationships = name("relationships")
            val rel = name("rel")

            match(
                node("Thing")
                    .withProperties("id", parameter("id"))
                    .named(n)
            )
                .call("apoc.path.subgraphAll")
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

    override fun findAllByObjectAndPredicate(
        objectId: ThingId,
        predicateId: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        findAllFilteredAndPaged(
            parameters = mapOf(
                "objectId" to objectId.value,
                "predicateId" to predicateId.value
            ),
            pageable = pageable
        ) { _, relation, `object` ->
            `object`.property("id").eq(parameter("objectId"))
                .and(relation.property("predicate_id").eq(parameter("predicateId")))
        }

    override fun findAllBySubjectAndPredicate(
        subjectId: ThingId,
        predicateId: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        findAllFilteredAndPaged(
            parameters = mapOf(
                "subjectId" to subjectId.value,
                "predicateId" to predicateId.value
            ),
            pageable = pageable
        ) { subject, relation, _ ->
            subject.property("id").eq(parameter("subjectId"))
                .and(relation.property("predicate_id").eq(parameter("predicateId")))
        }

    override fun findAllByPredicateIdAndLabel(
        predicateId: ThingId,
        literal: String,
        pageable: Pageable
    ): Page<GeneralStatement> =
        findAllFilteredAndPaged(
            pageable = pageable,
            parameters = mapOf(
                "predicateId" to predicateId.value,
                "literal" to literal
            ),
            `object` = node("Thing", "Literal")
        ) { _, relation, `object` ->
            relation.property("predicate_id").eq(parameter("predicateId"))
                .and(`object`.property("label").eq(parameter("literal")))
        }

    override fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: ThingId,
        literal: String,
        subjectClass: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        findAllFilteredAndPaged(
            pageable = pageable,
            parameters = mapOf(
                "predicateId" to predicateId.value,
                "literal" to literal,
                "predicateId" to predicateId.value,
                "subjectClass" to subjectClass.value
            ),
            `object` = node("Thing", "Literal")
                .withProperties("label", parameter("literal"))
        ) { subject, relation, _ ->
            relation.property("predicate_id").eq(parameter("predicateId"))
                .and(parameter("subjectClass").`in`(labels(subject)))
        }

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
                    .call("apoc.path.subgraphAll")
                    .withArgs(n, parameter("config"))
                    .yield(relationships)
                    .with(relationships)
                    .unwind(relationships).`as`(rel)
                    .with(startNode(rel).`as`("sub"), rel.`as`("rel"), endNode(rel).`as`("obj"))
                    .orderBy(rel.property("created_at").descending())
                    .returningWithSortableFields("rel", "sub", "obj")
                    .orderBy(sort)
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
                .withProperties("predicate_id", literalOf<String>(ObjectService.ID_CONTRIBUTION_PREDICATE))
                .relationshipTo(node("Literal").named(doi), RELATED)
                .properties("predicate_id", literalOf<String>(ObjectService.ID_DOI_PREDICATE))
            match(relations).returning(doi)
        }
        .withParameters("id" to id.value)
        .mappedBy(LiteralMapper("doi"))
        .one()

    override fun countPredicateUsage(id: ThingId): Long = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val r1 = node("Thing")
                .relationshipTo(node("Thing"), RELATED)
                .withProperties("predicate_id", parameter("id"))
            val r2 = node("Predicate")
                .withProperties("id", parameter("id"))
                .relationshipBetween(node("Thing"), RELATED)
            val cnt = name("cnt")
            call(
                unionAll(
                    optionalMatch(r1)
                        .returning(countDistinct(r1.asExpression()).`as`(cnt))
                        .build(),
                    optionalMatch(r2)
                        .where(r2.property("predicate_id").ne(literalOf<String>("description")))
                        .returning(countDistinct(r2.asExpression()).`as`(cnt))
                        .build()
                )
            )
            .with(cnt)
            .returning(sum(cnt.asExpression()))
        }
        .withParameters(
            "id" to id.value
        )
        .fetchAs<Long>()
        .one()
        .orElse(0)

    override fun findByDOI(doi: String): Optional<Resource> = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val p = node("Resource").named("p")
            val l = name("l")
            match(
                p.relationshipTo(node("Literal").named(l), RELATED)
                    .withProperties("predicate_id", literalOf<String>(ObjectService.ID_DOI_PREDICATE))
            ).where(
                toUpper(l.property("label")).eq(toUpper(parameter("doi")))
            ).returning(p)
                .limit(1)
        }
        .withParameters("doi" to doi)
        .mappedBy(ResourceMapper("p"))
        .one()

    override fun findPaperByDOI(doi: String): Optional<Resource> = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val p = name("p")
            val paper = paperNode()
                .named(p)
            val l = name("l")
            match(
                paper.relationshipTo(node("Literal").named(l), RELATED)
                    .withProperties("predicate_id", literalOf<String>(ObjectService.ID_DOI_PREDICATE))
            ).where(
                paper.hasLabels("PaperDeleted").not()
                    .and(toUpper(l.property("label")).eq(toUpper(parameter("doi"))))
            ).returning(p)
                .limit(1)
        }
        .withParameters("doi" to doi)
        .mappedBy(ResourceMapper("p"))
        .one()

    override fun findAllPapersByDOI(doi: String, pageable: Pageable): Page<Resource> = CypherQueryBuilder(neo4jClient)
        .withCommonQuery {
            val paper = paperNode().named("p")
            val l = name("l")
            match(
                paper.relationshipTo(node("Literal").named(l), RELATED)
                    .withProperties("predicate_id", literalOf<String>(ObjectService.ID_DOI_PREDICATE))
            ).where(
                paper.hasLabels("PaperDeleted").not()
                    .and(toUpper(l.property("label")).eq(toUpper(parameter("doi"))))
            )
        }
        .withQuery { commonQuery ->
            commonQuery.returningDistinct(name("p"))
        }
        .countDistinctOver("p")
        .withParameters(
            "doi" to doi
        )
        .mappedBy(ResourceMapper("p"))
        .fetch(pageable)

    override fun findProblemsByObservatoryId(id: ObservatoryId, pageable: Pageable): Page<Resource> =
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
                    "labelFilter" to "-ResearchField|-ResearchProblem|-Paper"
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
                ).call("apoc.path.subgraphAll")
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
                    "labelFilter" to "-ResearchField|-ResearchProblem|-Paper"
                )
                val n = name("n")
                val relationships = name("relationships")
                val rel = name("rel")
                val nodes = name("nodes")
                val node = name("node")
                val createdBy = name("createdBy")
                val ms = name("ms")
                val timestamp = name("timestamp")
                val edit = listOf(
                    createdBy,
                    call("apoc.date.format").withArgs(
                        ms,
                        literalOf<String>("ms"),
                        literalOf<String>("yyyy-MM-dd'T'HH:mm:ssXXX")
                    ).asFunction()
                ).`as`("edit")

                match(
                    node("Resource")
                        .withProperties("id", parameter("id"))
                        .named(n)
                ).call("apoc.path.subgraphAll")
                    .withArgs(n, asExpression(apocConfiguration))
                    .yield(relationships)
                    .with(relationships, n)
                    .unwind(relationships).`as`(rel)
                    .withDistinct(collect(rel).add(collect(endNode(rel))).add(collect(n)).`as`(nodes), n.asExpression())
                    .unwind(nodes).`as`(node)
                    .with(node, n)
                    .where(
                        node.property("created_by").isNotNull
                            .and(node.property("created_at").isNotNull)
                            .and(node.property("created_at").gte(n.property("created_at")))
                    )
                    .with(
                        node.property("created_by").`as`(createdBy),
                        call("apoc.text.regreplace").withArgs(
                            node.property("created_at"),
                            literalOf<String>("""^(\d+-\d+-\d+T\d+:\d+):\d+(?:\.\d+)?(.*)$"""),
                            literalOf<String>("$1:00$2")
                        ).asFunction().`as`(timestamp)
                    ).with(
                        createdBy,
                        call("apoc.date.parse").withArgs(
                            timestamp,
                            literalOf<String>("ms"),
                            literalOf<String>("yyyy-MM-dd'T'HH:mm:ssXXX")
                        ).asFunction().`as`(ms).asExpression()
                    ).withDistinct(edit)
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
            // TODO: Can be changed to ContributorId and OffsetDateTime once old adapters are deleted
            .mappedBy { _, record -> ResourceContributor(record["createdBy"].asString(), record["createdAt"].asString()) }
            .fetch(pageable)

    override fun checkIfResourceHasStatements(id: ThingId): Boolean = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val n = node("Resource")
                .withProperties("id", parameter("id"))
                .named("n")
            match(n).returning(exists(n.relationshipBetween(node("Thing"), RELATED)))
        }
        .withParameters("id" to id.value)
        .fetchAs<Boolean>()
        .one()
        .orElse(false)

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

    override fun findBySubjectIdAndPredicateIdAndObjectId(
        subjectId: ThingId,
        predicateId: ThingId,
        objectId: ThingId
    ): Optional<GeneralStatement> = CypherQueryBuilder(neo4jClient)
        .withQuery {
            val r = name("rel")
            val subject = node("Thing")
            val `object` = node("Thing")
            match(
                subject.relationshipTo(`object`, RELATED).named(r)
            ).where(
                r.property("predicate_id").eq(parameter("predicateId"))
                    .and(subject.property("id").eq(parameter("subjectId")))
                    .and(`object`.property("id").eq(parameter("objectId")))
            )
                .returningWithSortableFields(r, subject.asExpression(), `object`.asExpression())
                .limit(1)
        }
        .withParameters(
            "subjectId" to subjectId.value,
            "predicateId" to predicateId.value,
            "objectId" to objectId.value
        )
        .mappedBy(StatementMapper(predicateRepository))
        .one()

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
            "relationshipFilter" to ">",
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

    private fun StatementBuilder.ExposesWith.returningWithSortableFields(
        relation: String,
        subject: String,
        `object`: String
    ): StatementBuilder.OngoingReadingAndReturn =
        returningWithSortableFields(name(relation), name(subject), name(`object`))

    private fun StatementBuilder.ExposesWith.returningWithSortableFields(
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
