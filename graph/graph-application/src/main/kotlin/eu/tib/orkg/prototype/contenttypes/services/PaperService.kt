package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.contenttypes.api.Identifiers
import eu.tib.orkg.prototype.contenttypes.api.PaperUseCases
import eu.tib.orkg.prototype.contenttypes.application.PaperNotFound
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.domain.model.Paper
import eu.tib.orkg.prototype.contenttypes.domain.model.PublicationInfo
import eu.tib.orkg.prototype.contenttypes.domain.model.toContributions
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class PaperService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository
) : PaperUseCases {
    override fun findById(id: ThingId): Optional<Paper> =
        resourceRepository.findPaperById(id)
            .map { it.toPaper() }

    override fun findAll(pageable: Pageable): Page<Paper> =
        resourceRepository.findAllByClass(Classes.paper, pageable)
            .pmap { it.toPaper() }

    override fun findAllByDOI(doi: String, pageable: Pageable): Page<Paper> =
        statementRepository.findAllPapersByDOI(doi, pageable)
            .pmap { it.toPaper() }

    override fun findAllByTitle(title: String, pageable: Pageable): Page<Paper> =
        resourceRepository.findAllByClassAndLabel(Classes.paper, SearchString.of(title, exactMatch = true), pageable)
            .pmap { it.toPaper() }

    override fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<Paper> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> resourceRepository.findAllListedPapers(pageable)
            VisibilityFilter.NON_FEATURED -> resourceRepository.findAllPapersByVisibility(Visibility.DEFAULT, pageable)
            VisibilityFilter.UNLISTED -> resourceRepository.findAllPapersByVisibility(Visibility.UNLISTED, pageable)
            VisibilityFilter.FEATURED -> resourceRepository.findAllPapersByVisibility(Visibility.FEATURED, pageable)
            VisibilityFilter.DELETED -> resourceRepository.findAllPapersByVisibility(Visibility.DELETED, pageable)
        }.pmap { it.toPaper() }

    override fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<Paper> =
        resourceRepository.findAllByClassAndCreatedBy(Classes.paper, contributorId, pageable)
            .pmap { it.toPaper() }

    override fun findAllContributorsByPaperId(id: ThingId, pageable: Pageable): Page<ContributorId> =
        resourceRepository.findPaperById(id)
            .map { statementRepository.findAllContributorsByResourceId(id, pageable) }
            .orElseThrow { PaperNotFound(id) }

    private fun Resource.toPaper(): Paper {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL).content
            .withoutObjectsHavingBlankLabels()
        return Paper(
            id = id,
            title = label,
            researchFields = statements.wherePredicate(Predicates.hasResearchField)
                .objectIdsWithLabel()
                .sortedBy { it.id },
            identifiers = statements.mapIdentifiers(Identifiers.paper),
            publicationInfo = PublicationInfo.from(statements),
            authors = statements.wherePredicate(Predicates.hasAuthor).objects()
                .filter { it is Resource || it is Literal }
                .pmap { it.toAuthor() },
            contributions = statements.toContributions(),
            observatories = listOf(observatoryId),
            organizations = listOf(organizationId),
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            verified = verified ?: false,
            visibility = visibility
        )
    }

    private fun Thing.toAuthor(): Author {
        return when (this) {
            is Resource -> toAuthor()
            is Literal -> toAuthor()
            else -> throw IllegalStateException("""Cannot convert "$id" to author. This is a bug!""")
        }
    }

    private fun Resource.toAuthor(): Author {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL).content
            .filter { it.`object`.label.isNotBlank() }
        return Author(
            id = id,
            name = label,
            identifiers = statements.mapIdentifiers(Identifiers.author),
            homepage = statements.wherePredicate(Predicates.hasWebsite).firstObjectLabel()
        )
    }

    private fun Literal.toAuthor() = Author(
        id = null,
        name = label,
        identifiers = emptyMap(),
        homepage = null
    )
}
