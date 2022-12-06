package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.contenttypes.domain.Paper
import eu.tib.orkg.prototype.contenttypes.spi.PaperRepository
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.application.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

interface PaperUseCases : RetrievePaperUseCase

interface RetrievePaperUseCase {
    fun findPaper(id: ResourceId): Optional<PaperResponse>
}

private val PAPER_DELETED_CLASS_ID = ClassId("PaperDeleted")
private val HAS_RESEARCH_FIELD = PredicateId("P30")
private val HAS_CONTRIBUTION = PredicateId("P31")
private val HAS_PUBLICATION_YEAR = PredicateId("P29")
private val HAS_PUBLICATION_MONTH = PredicateId("P28")
private val HAS_DOI = PredicateId("P26")
private val HAS_AUTHOR = PredicateId("P27")
private val HAS_VENUE = PredicateId("HAS_VENUE")

class PaperService(
    private val repository: PaperRepository,
    private val statementRepository: StatementRepository
) : PaperUseCases {
    override fun findPaper(id: ResourceId) =
        repository.findById(id).map(::expand)

    fun expand(paper: Paper): PaperResponse {
        val resource = paper.resource
        val statements = StatementHelper(resource.id!!, statementRepository).gatherStatements()
        return PaperResponse(
            title = resource.label,
            id = resource.id,
            researchField = statements.first(HAS_RESEARCH_FIELD).get().id!!,
            identifiers = PaperIdentifiers(
                doi = statements.first(HAS_DOI).get().label
            ),
            publicationInfo = PublicationInfo(
                publishedMonth = statements.first(HAS_PUBLICATION_MONTH).get().label?.toInt(),
                publishedYear = statements.first(HAS_PUBLICATION_YEAR).get().label.toLong(),
                publishedIn = statements.first(HAS_VENUE).get().label, // TODO Resource ID maybe?
                downloadUrl = null
            ),
            authors = statements.all(HAS_AUTHOR).get().map { it.thingId },
            contributors = listOf(), // TODO how to get all?
            contributions = statements.all(HAS_CONTRIBUTION).get().map { it.id!! },
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

    fun first(predicateId: PredicateId): Optional<Thing> {
        val statement = statements[predicateId]

        if (statement.isNullOrEmpty()) {
            return Optional.empty()
        }

        return Optional.of(statement.first().`object`)
    }

    fun all(predicateId: PredicateId): Optional<List<Thing>> {
        val statement = statements[predicateId]

        if (statement.isNullOrEmpty()) {
            return Optional.empty()
        }

        return Optional.of(statement.map { it.`object` })
    }
}

data class PaperResponse(
    val id: ThingId,
    val title: String,
    val researchField: ResourceId,
    val identifiers: PaperIdentifiers,
    val publicationInfo: PublicationInfo,
    val authors: List<ThingId>, // TODO ThingId? and/or Set?
    val contributors: List<ContributorId>,
    val contributions: List<ResourceId>,
    val observatories: List<ObservatoryId>,
    val organizations: List<OrganizationId>,
    val extractionMethod: ExtractionMethod,
    val createdAt: OffsetDateTime,
    val createdBy: ContributorId,
    val featured: Boolean,
    val unlisted: Boolean,
    val verified: Boolean,
    val deleted: Boolean
)

data class PublicationInfo(
    val publishedMonth: Int?,
    val publishedYear: Long?,
    val publishedIn: String?,
    val downloadUrl: String?
)

data class PaperIdentifiers(
    val doi: String?
)
