package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.contenttypes.api.Identifiers
import eu.tib.orkg.prototype.contenttypes.api.PaperUseCases
import eu.tib.orkg.prototype.contenttypes.application.PaperNotFound
import eu.tib.orkg.prototype.contenttypes.domain.model.Paper
import eu.tib.orkg.prototype.contenttypes.domain.model.PublicationInfo
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.SearchString
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
        statementRepository.findAllBySubjectClassAndDOI(Classes.paper, doi, pageable)
            .pmap { it.toPaper() }

    override fun findAllByTitle(title: String, pageable: Pageable): Page<Paper> =
        resourceRepository.findAllByClassAndLabel(Classes.paper, SearchString.of(title, exactMatch = true), pageable)
            .pmap { it.toPaper() }

    override fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<Paper> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> resourceRepository.findAllListedByClass(Classes.paper, pageable)
            VisibilityFilter.NON_FEATURED -> resourceRepository.findAllByClassAndVisibility(Classes.paper, Visibility.DEFAULT, pageable)
            VisibilityFilter.UNLISTED -> resourceRepository.findAllByClassAndVisibility(Classes.paper, Visibility.UNLISTED, pageable)
            VisibilityFilter.FEATURED -> resourceRepository.findAllByClassAndVisibility(Classes.paper, Visibility.FEATURED, pageable)
            VisibilityFilter.DELETED -> resourceRepository.findAllByClassAndVisibility(Classes.paper, Visibility.DELETED, pageable)
        }.pmap { it.toPaper() }

    override fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<Paper> =
        resourceRepository.findAllByClassAndCreatedBy(Classes.paper, contributorId, pageable)
            .pmap { it.toPaper() }

    override fun findAllContributorsByPaperId(id: ThingId, pageable: Pageable): Page<ContributorId> =
        resourceRepository.findPaperById(id)
            .map { statementRepository.findAllContributorsByResourceId(id, pageable) }
            .orElseThrow { PaperNotFound(id) }

    private fun Resource.toPaper(): Paper {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL)
            .content
            .withoutObjectsWithBlankLabels()
        return Paper(
            id = id,
            title = label,
            researchFields = statements.wherePredicate(Predicates.hasResearchField).objectIdsAndLabel(),
            identifiers = statements.mapIdentifiers(Identifiers.paper),
            publicationInfo = PublicationInfo.from(statements),
            authors = statements.authors(statementRepository),
            contributions = statements.wherePredicate(Predicates.hasContribution).objectIdsAndLabel(),
            observatories = listOf(observatoryId),
            organizations = listOf(organizationId),
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            verified = verified ?: false,
            visibility = visibility
        )
    }
}
