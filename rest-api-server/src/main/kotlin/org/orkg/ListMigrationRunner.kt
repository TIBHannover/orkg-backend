package org.orkg

import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.List
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.input.CreatePredicateUseCase
import org.orkg.graph.input.UnsafePredicateUseCases
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.OffsetDateTime

private const val CHUNK_SIZE = 10_000

@Component
@Profile("listMigrations")
class ListMigrationRunner(
    private val listRepository: ListRepository,
    private val resourceRepository: ResourceRepository,
    private val unsafePredicateUseCases: UnsafePredicateUseCases,
    private val statementRepository: StatementRepository,
    private val predicateRepository: PredicateRepository,
    private val neo4jClient: Neo4jClient,
    private val clock: Clock,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Autowired
    private lateinit var context: ConfigurableApplicationContext

    override fun run(args: ApplicationArguments?) {
        logger.info("Starting list migration...")
        neo4jClient.query("""MATCH (:LiteratureList)-[:RELATED {predicate_id: "HasSection"}]->(:ListSection)-[:RELATED {predicate_id: "HasEntry"}]->(:Resource)-[r:RELATED {predicate_id: "HasPaper"}]->(:Resource) SET r.predicate_id = "HasLink" RETURN COUNT(r)""")
            .run()
        val hasAuthorsPredicate = findOrCreatePredicate(
            label = "authors",
            id = Predicates.hasAuthors
        )
//        val hasSectionsPredicate = findOrCreatePredicate(
//            label = "sections",
//            id = Predicates.hasSections
//        )
//        val hasEntitiesPredicate = findOrCreatePredicate(
//            label = "entities",
//            id = Predicates.hasEntities
//        )
        forEachResource(Classes.paper) {
            migrateStatementsToList(it, Predicates.hasAuthor, hasAuthorsPredicate, "authors list")
        }
        forEachResource(Classes.paperDeleted) {
            migrateStatementsToList(it, Predicates.hasAuthor, hasAuthorsPredicate, "authors list")
        }
        forEachResource(Classes.paperVersion) {
            migrateStatementsToList(it, Predicates.hasAuthor, hasAuthorsPredicate, "authors list")
        }
        forEachResource(Classes.smartReview) {
            migrateStatementsToList(it, Predicates.hasAuthor, hasAuthorsPredicate, "authors list")
        }
        forEachResource(Classes.literatureList) {
            migrateStatementsToList(it, Predicates.hasAuthor, hasAuthorsPredicate, "authors list")
        }
        forEachResource(Classes.comparison) {
            migrateStatementsToList(it, Predicates.hasAuthor, hasAuthorsPredicate, "authors list")
        }
        forEachResource(Classes.deletedComparison) {
            migrateStatementsToList(it, Predicates.hasAuthor, hasAuthorsPredicate, "authors list")
        }
        forEachResource(Classes.visualization) {
            migrateStatementsToList(it, Predicates.hasAuthor, hasAuthorsPredicate, "authors list")
        }
//        forEachResource(Classes.literatureList) {
//            migrateLiteratureList(it, hasSectionsPredicate)
//        }
//        forEachResource(Classes.contributionSmartReview) {
//            migrateStatementsToList(it, Predicates.hasSection, hasSectionsPredicate, "Sections")
//        }
//        forEachResource(Classes.ontologySection) {
//            migrateStatementsToList(it, Predicates.hasEntity, hasEntitiesPredicate, "Entities")
//        }
        context.close()
        logger.info("List migration complete")
    }

    private fun migrateStatementsToList(resource: Resource, oldPredicateId: ThingId, newPredicate: Predicate, label: String) {
        val statements = statementRepository.findAll(
            subjectId = resource.id,
            predicateId = oldPredicateId,
            pageable = PageRequests.ALL
        )
        if (statements.isEmpty) {
            return
        }
        val objects = statements
            .sortedBy { it.createdAt }
            .map { it.`object`.id }
        val listId = listRepository.nextIdentity()
        listRepository.save(
            List(
                id = listId,
                label = label,
                elements = objects,
                createdAt = OffsetDateTime.now(clock),
                createdBy = ContributorId.UNKNOWN
            ),
            ContributorId.UNKNOWN
        )
        statementRepository.deleteByStatementIds(statements.mapTo(mutableSetOf()) { it.id })
        statementRepository.save(
            GeneralStatement(
                id = statementRepository.nextIdentity(),
                subject = resource,
                predicate = newPredicate,
                `object` = resourceRepository.findById(listId).get(),
                createdBy = ContributorId.UNKNOWN,
                createdAt = OffsetDateTime.now(clock)
            )
        )
    }

    private fun migrateLiteratureList(resource: Resource, newPredicate: Predicate) {
        val sectionStatements = statementRepository.findAll(
            subjectId = resource.id,
            predicateId = Predicates.hasSection,
            pageable = PageRequests.ALL
        )
        val sections = sectionStatements
            .sortedBy { it.createdAt }
            .map { sectionStatement ->
                val entryStatements = statementRepository.findAll(
                    subjectId = sectionStatement.`object`.id,
                    predicateId = Predicates.hasEntry,
                    pageable = PageRequests.ALL
                )
                resourceRepository.deleteById(sectionStatement.`object`.id)
                if (entryStatements.content.isEmpty()) {
                    return@map null
                }
                val entries = entryStatements
                    .sortedBy { it.createdAt }
                    .map { it.`object`.id }
                entryStatements.forEach {
                    statementRepository.delete(it)
                    resourceRepository.deleteById(it.`object`.id)
                }
                val listId = listRepository.nextIdentity()
                listRepository.save(
                    List(
                        id = listId,
                        label = "Entries",
                        elements = entries,
                        createdBy = (sectionStatement.`object` as Resource).createdBy,
                        createdAt = OffsetDateTime.now(clock)
                    ),
                    (sectionStatement.`object` as Resource).createdBy
                )
                return@map listId
            }.filterNotNull()
        if (sections.isEmpty()) {
            return
        }
        val listId = listRepository.nextIdentity()
        listRepository.save(
            List(
                id = listId,
                label = "Sections",
                elements = sections,
                createdBy = ContributorId.UNKNOWN,
                createdAt = OffsetDateTime.now(clock)
            ),
            ContributorId.UNKNOWN
        )
        statementRepository.deleteByStatementIds(sectionStatements.mapTo(mutableSetOf()) { it.id })
        statementRepository.save(
            GeneralStatement(
                id = statementRepository.nextIdentity(),
                subject = resource,
                predicate = newPredicate,
                `object` = resourceRepository.findById(listId).get(),
                createdBy = ContributorId.UNKNOWN,
                createdAt = OffsetDateTime.now(clock)
            )
        )
    }

    private fun findOrCreatePredicate(label: String, id: ThingId): Predicate =
        predicateRepository.findById(id).orElseGet {
            val predicateId = unsafePredicateUseCases.create(
                CreatePredicateUseCase.CreateCommand(
                    id = id,
                    contributorId = ContributorId.UNKNOWN,
                    label = label
                )
            )
            predicateRepository.findById(predicateId).get()
        }

    private fun forEachResource(classId: ThingId, consumer: (Resource) -> Unit) {
        logger.info("Migrating resources of class $classId...")
        var page = resourceRepository.findAll(
            includeClasses = setOf(classId),
            pageable = PageRequest.of(0, CHUNK_SIZE)
        )
        page.content.forEach(consumer)
        while (page.hasNext()) {
            page = resourceRepository.findAll(
                includeClasses = setOf(classId),
                pageable = PageRequest.of(page.number + 1, CHUNK_SIZE)
            )
            page.content.forEach(consumer)
        }
        logger.info("Migration of resources of class $classId complete")
    }
}
