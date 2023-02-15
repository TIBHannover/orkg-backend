package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatementIdGenerator
import eu.tib.orkg.prototype.statements.api.BundleConfiguration
import eu.tib.orkg.prototype.statements.api.RetrieveStatementUseCase.PredicateUsageCount
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.ObjectService
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceContributor
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.*
import org.neo4j.cypherdsl.core.Condition
import org.neo4j.cypherdsl.core.Cypher.anyNode
import org.neo4j.cypherdsl.core.Cypher.asExpression
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Cypher.name
import org.neo4j.cypherdsl.core.Cypher.node
import org.neo4j.cypherdsl.core.Cypher.optionalMatch
import org.neo4j.cypherdsl.core.Cypher.returning
import org.neo4j.cypherdsl.core.Cypher.union
import org.neo4j.cypherdsl.core.FunctionInvocation
import org.neo4j.cypherdsl.core.Functions.collect
import org.neo4j.cypherdsl.core.Functions.count
import org.neo4j.cypherdsl.core.Functions.countDistinct
import org.neo4j.cypherdsl.core.Predicates.exists
import org.neo4j.cypherdsl.core.ResultStatement
import org.neo4j.cypherdsl.core.StatementBuilder
import org.neo4j.cypherdsl.core.SymbolicName
import org.neo4j.cypherdsl.core.utils.Assertions
import org.neo4j.driver.Value
import org.neo4j.driver.types.MapAccessor
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.Neo4jClient.RecordFetchSpec
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.data.neo4j.core.mappedBy
import org.springframework.stereotype.Component
import org.neo4j.cypherdsl.core.Node as CNode
import org.neo4j.cypherdsl.core.Relationship as CRelationship

private const val RELATED = "RELATED"

@Component
class SpringDataNeo4jStatementAdapter(
    private val neo4jStatementIdGenerator: Neo4jStatementIdGenerator,
    private val predicateRepository: PredicateRepository,
    override val neo4jClient: Neo4jClient
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
        val subject = node("Thing")
        val `object` = node("Thing")
        val query = match(subject).where(
                subject.property("class_id").eq(literalOf<String>(statement.subject.thingId.value))
                    .or(subject.property("predicate_id").eq(literalOf<String>(statement.subject.thingId.value)))
                    .or(subject.property("resource_id").eq(literalOf<String>(statement.subject.thingId.value)))
                    .or(subject.property("literal_id").eq(literalOf<String>(statement.subject.thingId.value)))
            ).match(`object`).where(
                `object`.property("class_id").eq(literalOf<String>(statement.`object`.thingId.value))
                    .or(`object`.property("predicate_id").eq(literalOf<String>(statement.`object`.thingId.value)))
                    .or(`object`.property("resource_id").eq(literalOf<String>(statement.`object`.thingId.value)))
                    .or(`object`.property("literal_id").eq(literalOf<String>(statement.`object`.thingId.value)))
            ).create(
                subject.relationshipTo(`object`, RELATED).withProperties(
                    "statement_id", literalOf<String>(statement.id?.value),
                    "predicate_id", literalOf<String>(statement.predicate.id?.value),
                    "created_by", literalOf<String>(statement.createdBy.value.toString()),
                    "created_at", literalOf<String>(statement.createdAt?.format(ISO_OFFSET_DATE_TIME))
                )
            ).build()
        neo4jClient.query(query.cypher).run()
    }

    override fun count(): Long {
        val r = name("r")
        val query = match(anyNode().relationshipTo(anyNode(), RELATED).named(r))
            .returning(count(r))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs<Long>()
            .one() ?: 0
    }

    override fun delete(statement: GeneralStatement) = deleteByStatementId(statement.id!!)

    override fun deleteByStatementId(id: StatementId) {
        val relation = anyNode().relationshipTo(anyNode(), RELATED)
            .named("r")
        val query = match(relation)
            .where(relation.property("statement_id").eq(literalOf<String>(id.value)))
            .delete(relation)
            .build()
        neo4jClient.query(query.cypher).run()
    }

    override fun deleteAll() {
        val r = name("r")
        val query = match(
            anyNode().relationshipTo(anyNode(), RELATED)
                .named(r)
            ).delete(r)
            .build()
        neo4jClient.query(query.cypher).run()
    }

    override fun findAll(pageable: Pageable): Page<GeneralStatement> {
        val r = name("r")
        val subject = anyNode()
        val `object` = anyNode()
        val match = match(subject.relationshipTo(`object`, RELATED).named(r))
        val query = match
            .returning(r, subject.`as`("s"), `object`.`as`("o"))
            .build(pageable)
        val countQuery = match
            .returning(count(r))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs(GeneralStatement::class.java)
            .mappedBy(StatementMapper(predicateRepository))
            .paged(pageable, countQuery)
    }

    override fun countStatementsAboutResource(id: ResourceId): Long {
        val r = name("r")
        val subject = node("Thing")
        val `object` = node("Resource")
        val query = match(
                subject.relationshipTo(`object`, RELATED)
                    .named(r)
            ).where(`object`.property("resource_id").eq(literalOf<String>(id.value)))
            .returning(count(r))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs<Long>()
            .one() ?: 0
    }

    override fun countStatementsAboutResources(resourceIds: Set<ResourceId>): Map<ResourceId, Long> {
        val r = name("r")
        val subject = node("Thing")
        val `object` = node("Resource")
        val resourceId = `object`.property("resource_id")
        val id = name("id")
        val count = name("c")
        val query = match(
                subject.relationshipTo(`object`, RELATED)
                    .named(r)
            ).where(resourceId.`in`(literalOf<List<String>>(resourceIds.map { it.value })))
            .with(resourceId.`as`(id), count(r).`as`(count))
            .returning(id, count)
            .build()
        return neo4jClient.query(query.cypher)
            .mappedBy { _, record -> ResourceId(record[id].asString()) to record[count].asLong() }
            .all()
            .toMap()
    }

    override fun findByStatementId(id: StatementId): Optional<GeneralStatement> {
        val r = name("r")
        val subject = node("Thing")
        val `object` = node("Thing")
        val query = match(
                subject.relationshipTo(`object`, RELATED).withProperties(
                    "statement_id", literalOf<String>(id.value)
                ).named(r)
            ).returning(r, subject.`as`("s"), `object`.`as`("o"))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs(GeneralStatement::class.java)
            .mappedBy(StatementMapper(predicateRepository))
            .one()
    }

    override fun findAllBySubject(subjectId: String, pageable: Pageable): Page<GeneralStatement> =
        findAllFilteredAndPaged(pageable) { subject, _, _ ->
            val subjectIdLiteral = literalOf<String>(subjectId)
            subject.property("resource_id").eq(subjectIdLiteral)
                .or(subject.property("literal_id").eq(subjectIdLiteral))
                .or(subject.property("predicate_id").eq(subjectIdLiteral))
                .or(subject.property("class_id").eq(subjectIdLiteral))
        }

    override fun findAllByPredicateId(predicateId: PredicateId, pageable: Pageable): Page<GeneralStatement> =
        findAllFilteredAndPaged(pageable) { _, relation, _ ->
            relation.property("predicate_id").eq(literalOf<String>(predicateId.value))
        }

    override fun findAllByObject(objectId: String, pageable: Pageable): Page<GeneralStatement> =
        findAllFilteredAndPaged(pageable) { _, _, `object` ->
            val objectIdLiteral = literalOf<String>(objectId)
            `object`.property("resource_id").eq(objectIdLiteral)
                .or(`object`.property("literal_id").eq(objectIdLiteral))
                .or(`object`.property("predicate_id").eq(objectIdLiteral))
                .or(`object`.property("class_id").eq(objectIdLiteral))
        }

    override fun countByIdRecursive(id: String): Long {
        val subject = node("Thing")
        val `object` = anyNode()
        val idLiteral = literalOf<String>(id)
        val query = match(
                subject.relationshipTo(`object`).unbounded()
            ).where(
                subject.property("resource_id").eq(idLiteral)
                    .or(subject.property("literal_id").eq(idLiteral))
                    .or(subject.property("predicate_id").eq(idLiteral))
                    .or(subject.property("class_id").eq(idLiteral))
            ).returning(count(subject))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs<Long>()
            .one() ?: 0
    }

    override fun findAllByObjectAndPredicate(
        objectId: String,
        predicateId: PredicateId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        findAllFilteredAndPaged(pageable) { _, relation, `object` ->
            val objectIdLiteral = literalOf<String>(objectId)
            `object`.property("resource_id").eq(objectIdLiteral)
                .or(`object`.property("literal_id").eq(objectIdLiteral))
                .or(`object`.property("predicate_id").eq(objectIdLiteral))
                .or(`object`.property("class_id").eq(objectIdLiteral))
                .and(relation.property("predicate_id").eq(literalOf<String>(predicateId.value)))
        }

    override fun findAllBySubjectAndPredicate(
        subjectId: String,
        predicateId: PredicateId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        findAllFilteredAndPaged(pageable) { subject, relation, _ ->
            val subjectIdLiteral = literalOf<String>(subjectId)
            subject.property("resource_id").eq(subjectIdLiteral)
                .or(subject.property("literal_id").eq(subjectIdLiteral))
                .or(subject.property("predicate_id").eq(subjectIdLiteral))
                .or(subject.property("class_id").eq(subjectIdLiteral))
                .and(relation.property("predicate_id").eq(literalOf<String>(predicateId.value)))
        }

    override fun findAllByPredicateIdAndLabel(
        predicateId: PredicateId,
        literal: String,
        pageable: Pageable
    ): Page<GeneralStatement> =
        findAllFilteredAndPaged(
            pageable = pageable,
            `object` = node("Thing", "Literal")
        ) { _, relation, `object` ->
            relation.property("predicate_id").eq(literalOf<String>(predicateId.value))
                .and(`object`.property("label").eq(literalOf<String>(literal)))
        }

    override fun findAllByPredicateIdAndLabelAndSubjectClass(
        predicateId: PredicateId,
        literal: String,
        subjectClass: ThingId,
        pageable: Pageable
    ): Page<GeneralStatement> =
        findAllFilteredAndPaged(
            pageable = pageable,
            `object` = node("Thing", "Literal")
        ) { subject, relation, `object` ->
            relation.property("predicate_id").eq(literalOf<String>(predicateId.value))
                .and(`object`.property("label").eq(literalOf<String>(literal)))
                .and(subject.property("class_id").eq(literalOf<String>(subjectClass.value)))
        }

    override fun findAllBySubjects(subjectIds: List<String>, pageable: Pageable): Page<GeneralStatement> =
        findAllFilteredAndPaged(pageable) { subject, _, _ ->
            val subjectIdsLiteral = literalOf<List<String>>(subjectIds)
            subject.property("resource_id").`in`(subjectIdsLiteral)
                .or(subject.property("literal_id").`in`(subjectIdsLiteral))
                .or(subject.property("predicate_id").`in`(subjectIdsLiteral))
                .or(subject.property("class_id").`in`(subjectIdsLiteral))
        }

    override fun findAllByObjects(objectIds: List<String>, pageable: Pageable): Page<GeneralStatement> =
        findAllFilteredAndPaged(pageable) { _, _, `object` ->
            val objectIdsLiteral = literalOf<List<String>>(objectIds)
            `object`.property("resource_id").`in`(objectIdsLiteral)
                .or(`object`.property("literal_id").`in`(objectIdsLiteral))
                .or(`object`.property("predicate_id").`in`(objectIdsLiteral))
                .or(`object`.property("class_id").`in`(objectIdsLiteral))
        }

    override fun fetchAsBundle(id: String, configuration: BundleConfiguration): Iterable<GeneralStatement> {
        val n = name("n")
        val relationships = name("relationships")
        val rel = name("rel")
        val node = node("Thing").named(n)
        val idLiteral = literalOf<String>(id)
        val query = match(node)
            .where(
                node.property("resource_id").eq(idLiteral)
                    .or(node.property("literal_id").eq(idLiteral))
                    .or(node.property("class_id").eq(idLiteral))
                    .or(node.property("predicate_id").eq(idLiteral))
            ).call("apoc.path.subgraphAll")
            .withArgs(n, asExpression(configuration.toApocConfiguration()))
            .yield(relationships)
            .with(relationships)
            .unwind(relationships).`as`(rel)
            .returning(startNode(rel).`as`("s"), rel.`as`("r"), endNode(rel).`as`("o"))
            .orderBy(rel.property("created_at").descending())
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs(GeneralStatement::class.java)
            .mappedBy(StatementMapper(predicateRepository))
            .all()
    }

    override fun exists(id: StatementId): Boolean {
        val query = returning(
                exists(
                    anyNode().relationshipTo(anyNode(), RELATED)
                        .withProperties("statement_id", literalOf<String>(id.value))
                )
            ).build()
        return neo4jClient.query(query.cypher)
            .fetchAs<Boolean>()
            .one() ?: false
    }

    override fun countPredicateUsage(pageable: Pageable): Page<PredicateUsageCount> {
        val r = name("r")
        val c = name("c")
        val id = name("id")
        val match = match(anyNode().relationshipTo(anyNode(), RELATED).named(r))
        val query = match
            .returning(r.property("predicate_id").`as`(id), count(r).`as`(c))
            .orderBy(c.descending(), id.ascending())
            .build(pageable)
        val countQuery = match
            .returning(countDistinct(r.property("predicate_id")))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs(PredicateUsageCount::class.java)
            .mappedBy { _, record -> PredicateUsageCount(PredicateId(record[id].asString()), record[c].asLong()) }
            .paged(pageable, countQuery)
    }

    override fun findDOIByContributionId(id: ResourceId): Optional<Literal> {
        val doi = name("doi")
        val relations = node("Resource")
            .withProperties("resource_id", literalOf<String>(id.value))
            .relationshipFrom(node("Paper"), RELATED)
            .withProperties("predicate_id", literalOf<String>(ObjectService.ID_CONTRIBUTION_PREDICATE))
            .relationshipTo(node("Literal").named(doi), RELATED)
            .properties("predicate_id", literalOf<String>(ObjectService.ID_DOI_PREDICATE))
        val query = match(relations)
            .returning(doi)
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs(Literal::class.java)
            .mappedBy(LiteralMapper(doi))
            .one()
    }

    override fun countPredicateUsage(id: PredicateId): Long {
        val idLiteral = literalOf<String>(id.value)
        val r1 = name("r1")
        val r2 = name("r2")
        val relations = name("relations")
        val nodes = name("nodes")
        val query = optionalMatch(
                node("Thing")
                    .relationshipTo(node("Thing"), RELATED)
                    .named(r1)
                    .withProperties("predicate_id", idLiteral))
            .optionalMatch(
                node("Predicate")
                    .withProperties("predicate_id", idLiteral)
                    .relationshipBetween(node("Thing"), RELATED)
                    .named(r2)
            ).with(countDistinct(r1).`as`(relations), countDistinct(r2).`as`(nodes))
            .returning(nodes.add(relations))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs<Long>()
            .one() ?: 0
    }

    override fun findByDOI(doi: String): Optional<Resource> {
        val p = name("p")
        val paper = node("Paper")
            .named(p)
        val literal = node("Literal")
            .withProperties("label", literalOf<String>(doi))
        val query = match(
                paper.relationshipTo(literal, RELATED)
                    .withProperties("predicate_id", literalOf<String>(ObjectService.ID_DOI_PREDICATE))
            ).where(paper.hasLabels("PaperDeleted").not())
            .returning(p)
            .limit(1)
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs(Resource::class.java)
            .mappedBy(ResourceMapper(p))
            .one()
    }

    // TODO: Update endpoint to use pagination once we upgraded to Neo4j 4.0
    override fun findProblemsByObservatoryId(id: ObservatoryId): Iterable<Resource> {
        val problem = name("p")
        val idLiteral = literalOf<String>(id.value.toString())
        val query = union(
            match(
                node("Paper")
                    .withProperties("observatory_id", idLiteral)
                    .relationshipTo(node("Problem").named(problem))
                    .unbounded()
            ).returning(problem).build(),
            match(
                node("Problem")
                    .named(problem)
                    .withProperties("observatory_id", idLiteral)
            ).returning(problem).build()
        )
        return neo4jClient.query(query.cypher)
            .fetchAs(Resource::class.java)
            .mappedBy(ResourceMapper(problem))
            .all()
    }

    override fun findContributorsByResourceId(id: ResourceId, pageable: Pageable): Page<ResourceContributor> {
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
        val createdAt = name("createdAt")
        val match = match(
                node("Resource")
                    .named(n)
                    .withProperties("resource_id", literalOf<String>(id.value))
            ).call("apoc.path.subgraphAll")
            .withArgs(n, asExpression(apocConfiguration))
            .yield(relationships)
            .with(relationships, n)
            .unwind(relationships).`as`(rel)
            .withDistinct(collect(rel).add(collect(endNode(rel))).add(n).`as`(nodes))
            .unwind(nodes).`as`(node)
            .withDistinct(node.property("created_by").`as`(createdBy), node.property("created_at").`as`(createdAt))
        val query = match
            .returning(createdBy, createdAt)
            .orderBy(createdAt.descending())
            .build(pageable)
        val countQuery = match
            .returning(count(createdBy))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs(ResourceContributor::class.java)
            // TODO: Can be changed to ContributorId and OffsetDateTime once old adapters are deleted
            .mappedBy { _, record -> ResourceContributor(record[createdBy].asString(), record[createdAt].asString()) }
            .paged(pageable, countQuery)
    }

    override fun checkIfResourceHasStatements(id: ResourceId): Boolean {
        val n = node("Resource")
            .withProperties("resource_id", literalOf<String>(id.value))
            .named("n")
        val query = match(n)
            .returning(exists(n.relationshipBetween(node("Thing"), RELATED)))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs<Boolean>()
            .one() ?: false
    }

    override fun findProblemsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource> {
        val problem = name("p")
        val match = match(
                node("Comparison")
                    .withProperties("organization_id", literalOf<String>(id.value.toString()))
                    .relationshipTo(node("Contribution"), RELATED)
                    .withProperties("predicate_id", literalOf<String>("compareContribution"))
                    .relationshipTo(node("Problem").named(problem), RELATED)
                    .properties("predicate_id", literalOf<String>("P32"))
            )
        val query = match
            .returningDistinct(problem)
            .build(pageable)
        val countQuery = match
            .returning(countDistinct(problem))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs(Resource::class.java)
            .mappedBy(ResourceMapper(problem))
            .paged(pageable, countQuery)
    }

    private fun findAllFilteredAndPaged(
        pageable: Pageable,
        subject: CNode = node("Thing"),
        `object`: CNode = node("Thing"),
        filter: (subject: CNode, relationship: CRelationship, `object`: CNode) -> Condition
    ): Page<GeneralStatement> {
        val r = name("r")
        val relation = subject.relationshipTo(`object`, RELATED)
            .named(r)
        val condition = filter(subject, relation, `object`)
        val match = match(relation).where(condition)
        val query = match
            .returning(r, subject.`as`("s"), `object`.`as`("o"))
            .build(pageable)
        val countQuery = match
            .returning(count(r))
            .build()
        return neo4jClient.query(query.cypher)
            .fetchAs(GeneralStatement::class.java)
            .mappedBy(StatementMapper(predicateRepository))
            .paged(pageable, countQuery)
    }

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
}
