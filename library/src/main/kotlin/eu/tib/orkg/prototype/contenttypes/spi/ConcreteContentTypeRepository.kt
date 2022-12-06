package eu.tib.orkg.prototype.contenttypes.spi

import eu.tib.orkg.prototype.contenttypes.domain.ContentType
import eu.tib.orkg.prototype.contenttypes.domain.Paper
import eu.tib.orkg.prototype.contenttypes.domain.PaperIdentifiers
import eu.tib.orkg.prototype.contenttypes.domain.PublicationInfo
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

// TODO: maybe need
// resource repo -> fun findAllByAnyClass(classes: Iterable<String>): Page<Neo4jResource>
// adapter -> resourceRepo.findAllByAnyClass(setOf(Paper, ...)) // id + classes

/**
 * Repository to deal with all content types.
 */
interface ContentTypeRepository {
    fun findLatest(pageable: Pageable): Page<ContentType>
    fun findLatestFeatured(pageable: Pageable): Page<ContentType>
}

interface ConcreteContentTypeRepository<T : ContentType> {
    val resourceRepository: ResourceRepository
    val contentTypeClass: String

    fun findAllFeaturedIs(featured: Boolean, pageable: Pageable): Page<T> =
        resourceRepository.findAllFeaturedResourcesByClass(listOf(contentTypeClass), featured, pageable).map(::toContentType)

    fun findAllUnlistedIs(unlisted: Boolean, pageable: Pageable): Page<T> =
        resourceRepository.findAllUnlistedResourcesByClass(listOf(contentTypeClass), unlisted, pageable).map(::toContentType)

    fun findAll(pageable: Pageable): Page<T> =
        resourceRepository.findAllByClass(contentTypeClass, pageable).map(::toContentType)

    fun findById(id: ResourceId): Optional<T> =
        resourceRepository.findByResourceId(id).map(::toContentType)

    fun save(contentType: T) =
        resourceRepository.save(toResource(contentType))

    fun toContentType(resource: Resource): T

    fun toResource(contentType: T): Resource
}

interface PaperRepository : ConcreteContentTypeRepository<Paper> {
    fun findByDOI()
}

private val PAPER_DELETED_CLASS_ID = ClassId("PaperDeleted")
private val HAS_RESEARCH_FIELD = PredicateId("P30")
private val HAS_CONTRIBUTION = PredicateId("P31")
private val HAS_PUBLICATION_YEAR = PredicateId("P29")
private val HAS_PUBLICATION_MONTH = PredicateId("P28")
private val HAS_DOI = PredicateId("P26")
private val HAS_AUTHOR = PredicateId("P27")
private val HAS_VENUE = PredicateId("HAS_VENUE")

class PaperRepositoryAdapter(
    override val resourceRepository: ResourceRepository,
    val statementRepository: StatementRepository
) : PaperRepository {
    override val contentTypeClass: String = "Paper"

    override fun toContentType(resource: Resource): Paper {
        val statements = StatementHelper(resource.id!!, statementRepository).gatherStatements()
        return Paper(
            title = resource.label,
            id = contentTypeClass,
            researchField = statements.first<Resource>(HAS_RESEARCH_FIELD).get().id!!,
            identifiers = PaperIdentifiers(
                doi = statements.first<Literal>(HAS_DOI).get().label
            ),
            publicationInfo = PublicationInfo(
                publishedMonth = statements.first<Resource>(HAS_PUBLICATION_MONTH).get().label.toInt(),
                publishedYear = statements.first<Resource>(HAS_PUBLICATION_YEAR).get().label.toLong(),
                publishedIn = statements.first<Resource>(HAS_VENUE).get().label, // TODO Resource ID maybe?
                downloadUrl = null
            ),
            authors = statements.all<Thing>(HAS_AUTHOR).get().map { it.thingId },
            contributors = listOf(), // TODO how to get all?
            contributions = statements.all<Resource>(HAS_CONTRIBUTION).get().map { it.id!! },
            observatories = listOf(resource.observatoryId),
            organizations = listOf(resource.organizationId),
            extractionMethod = resource.extractionMethod,
            createdAt = resource.createdAt,
            createdBy = resource.createdBy,
            featured = resource.featured ?: false,
            unlisted = resource.unlisted ?: false,
            verified = resource.verified ?: false,
            deleted = PAPER_DELETED_CLASS_ID in resource.classes
        )
    }

    override fun toResource(contentType: Paper): Resource {
        TODO("Not yet implemented")
    }

    override fun findByDOI() {
        TODO()
    }
}

class StatementHelper(
    val resourceId: ResourceId,
    val statementRepository: StatementRepository
) {
    private val statements: MutableMap<PredicateId, MutableList<GeneralStatement>> = mutableMapOf()

    // TODO: Maybe use the statements directly from the resource NodeEntity
    fun gatherStatements(): StatementHelper {
        val statements = mutableListOf<GeneralStatement>()
        var page: Page<GeneralStatement> = statementRepository.findAllBySubject(resourceId.value, PageRequest.of(0, 10))
        statements.addAll(page)
        while (page.hasNext()) {
            page = statementRepository.findAllBySubject(resourceId.value, page.nextPageable())
            statements.addAll(page)
        }
        statements.groupByTo(this.statements) { it.predicate.id!! }
        return this
    }

    fun <T: Thing> first(predicateId: PredicateId): Optional<T> {
        val statement = statements[predicateId]

        if (statement.isNullOrEmpty()) {
            return Optional.empty()
        }

        return Optional.of(statement.first().`object` as T)
    }

    fun <T: Thing> all(predicateId: PredicateId): Optional<List<T>> {
        val statement = statements[predicateId]

        if (statement.isNullOrEmpty()) {
            return Optional.empty()
        }

        return Optional.of(statement.map { it.`object` as T })
    }
}
