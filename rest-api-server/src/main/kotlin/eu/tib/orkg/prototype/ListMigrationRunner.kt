package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.CreatePredicateUseCase
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.List
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.PredicateService
import eu.tib.orkg.prototype.statements.spi.ListRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.time.OffsetDateTime
import org.neo4j.ogm.session.SessionFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

private const val chunkSize = 10_000
private val UNKNOWN_CONTRIBUTOR = ContributorId.createUnknownContributor()

@Component
@Profile("listMigrations")
class ListMigrationRunner(
    private val listRepository: ListRepository,
    private val resourceRepository: ResourceRepository,
    private val predicateService: PredicateService,
    private val statementRepository: StatementRepository,
    private val predicateRepository: PredicateRepository,
    private val sessionFactory: SessionFactory
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @Autowired
    private lateinit var context: ConfigurableApplicationContext

    override fun run(args: ApplicationArguments?) {
        logger.info("Starting list migration...")
        with(sessionFactory.openSession()) {
            query("""MATCH (:LiteratureList)-[:RELATED {predicate_id: "HasSection"}]->(:ListSection)-[:RELATED {predicate_id: "HasEntry"}]->(:Resource)-[r:RELATED {predicate_id: "HasPaper"}]->(:Resource) SET r.predicate_id = "HasLink" RETURN COUNT(r)""", emptyMap<String, Any>())
        }
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
        forEachResource(Classes.review) {
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
        val statements = statementRepository.findAllBySubjectAndPredicate(
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
                createdAt = OffsetDateTime.now(),
                createdBy = UNKNOWN_CONTRIBUTOR
            ),
            UNKNOWN_CONTRIBUTOR
        )
        statementRepository.deleteByStatementIds(statements.mapTo(mutableSetOf()) { it.id!! })
        statementRepository.save(
            GeneralStatement(
                id = statementRepository.nextIdentity(),
                subject = resource,
                predicate = newPredicate,
                `object` = resourceRepository.findById(listId).get(),
                createdBy = UNKNOWN_CONTRIBUTOR,
                createdAt = OffsetDateTime.now()
            )
        )
    }

    private fun migrateLiteratureList(resource: Resource, newPredicate: Predicate) {
        val sectionStatements = statementRepository.findAllBySubjectAndPredicate(
            subjectId = resource.id,
            predicateId = Predicates.hasSection,
            pageable = PageRequests.ALL
        )
        val sections = sectionStatements
            .sortedBy { it.createdAt }
            .map { sectionStatement ->
                val entryStatements = statementRepository.findAllBySubjectAndPredicate(
                    subjectId = sectionStatement.`object`.id,
                    predicateId = Predicates.hasEntry,
                    pageable = PageRequests.ALL
                )
                resourceRepository.deleteById(sectionStatement.`object`.id)
                if (entryStatements.content.isEmpty()) {
                    return@map null
                }
                val objects = statementRepository.findAllBySubjects(
                    subjectIds = entryStatements.content.map { it.`object`.id },
                    pageable = PageRequests.ALL
                ).associateBy { it.subject.id }
                val entries = entryStatements
                    .sortedBy { it.createdAt }
                    .map { objects[it.`object`.id]!!.`object`.id }
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
                        createdAt = OffsetDateTime.now()
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
                createdBy = UNKNOWN_CONTRIBUTOR,
                createdAt = OffsetDateTime.now()
            ),
            UNKNOWN_CONTRIBUTOR
        )
        statementRepository.deleteByStatementIds(sectionStatements.mapTo(mutableSetOf()) { it.id!! })
        statementRepository.save(
            GeneralStatement(
                id = statementRepository.nextIdentity(),
                subject = resource,
                predicate = newPredicate,
                `object` = resourceRepository.findById(listId).get(),
                createdBy = UNKNOWN_CONTRIBUTOR,
                createdAt = OffsetDateTime.now()
            )
        )
    }

    private fun findOrCreatePredicate(label: String, id: ThingId): Predicate =
        predicateRepository.findById(id).orElseGet {
            val predicateId = predicateService.create(
                CreatePredicateUseCase.CreateCommand(
                    label = label,
                    id = id.value
                )
            )
            predicateRepository.findById(predicateId).get()
        }

    private fun forEachResource(classId: ThingId, consumer: (Resource) -> Unit) {
        logger.info("Migrating resources of class $classId...")
        var page = resourceRepository.findAllByClass(classId, PageRequest.of(0, chunkSize))
        page.content.forEach(consumer)
        while (page.hasNext()) {
            page = resourceRepository.findAllByClass(classId, PageRequest.of(page.number + 1, chunkSize))
            page.content.forEach(consumer)
        }
        logger.info("Migration of resources of class $classId complete")
    }
}
