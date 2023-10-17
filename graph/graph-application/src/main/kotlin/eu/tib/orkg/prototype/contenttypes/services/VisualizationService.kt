package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.contenttypes.api.VisualizationUseCases
import eu.tib.orkg.prototype.contenttypes.domain.model.Visualization
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldUseCase
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
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class VisualizationService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val researchFieldService: RetrieveResearchFieldUseCase
) : VisualizationUseCases {
    override fun findById(id: ThingId): Optional<Visualization> =
        resourceRepository.findById(id)
            .filter { it is Resource && Classes.visualization in it.classes }
            .map { it.toVisualization() }

    override fun findAll(pageable: Pageable): Page<Visualization> =
        resourceRepository.findAllByClass(Classes.visualization, pageable)
            .pmap { it.toVisualization() }

    override fun findAllByTitle(title: String, pageable: Pageable): Page<Visualization> =
        resourceRepository.findAllByClassAndLabel(Classes.visualization, SearchString.of(title, exactMatch = true), pageable)
            .pmap { it.toVisualization() }

    override fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<Visualization> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> resourceRepository.findAllListedByClass(Classes.visualization, pageable)
            VisibilityFilter.NON_FEATURED -> resourceRepository.findAllByClassAndVisibility(Classes.visualization, Visibility.DEFAULT, pageable)
            VisibilityFilter.UNLISTED -> resourceRepository.findAllByClassAndVisibility(Classes.visualization, Visibility.UNLISTED, pageable)
            VisibilityFilter.FEATURED -> resourceRepository.findAllByClassAndVisibility(Classes.visualization, Visibility.FEATURED, pageable)
            VisibilityFilter.DELETED -> resourceRepository.findAllByClassAndVisibility(Classes.visualization, Visibility.DELETED, pageable)
        }.pmap { it.toVisualization() }

    override fun findAllByResearchFieldAndVisibility(
        researchFieldId: ThingId,
        visibility: VisibilityFilter,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Visualization> =
        researchFieldService.findAllVisualizationsByResearchField(researchFieldId, visibility, includeSubfields, pageable)
            .pmap { it.toVisualization() }

    override fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<Visualization> =
        resourceRepository.findAllByClassAndCreatedBy(Classes.visualization, contributorId, pageable)
            .pmap { it.toVisualization() }

    private fun Resource.toVisualization(): Visualization {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL)
            .content
            .withoutObjectsWithBlankLabels()
        return Visualization(
            id = id,
            title = label,
            description = statements.wherePredicate(Predicates.description).firstObjectLabel(),
            authors = statements.authors(statementRepository),
            observatories = listOf(observatoryId),
            organizations = listOf(organizationId),
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            visibility = visibility
        )
    }
}
