package org.orkg.contenttypes.domain

import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.VisualizationState
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.visualization.VisualizationAuthorCreator
import org.orkg.contenttypes.domain.actions.visualization.VisualizationAuthorValidator
import org.orkg.contenttypes.domain.actions.visualization.VisualizationDescriptionCreator
import org.orkg.contenttypes.domain.actions.visualization.VisualizationResourceCreator
import org.orkg.contenttypes.input.RetrieveResearchFieldUseCase
import org.orkg.contenttypes.input.VisualizationUseCases
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.authors
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class VisualizationService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
    private val resourceService: ResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val listService: ListUseCases,
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

    override fun create(command: CreateVisualizationCommand): ThingId {
        val steps = listOf(
            ObservatoryValidator(observatoryRepository) { it.observatories },
            OrganizationValidator(organizationRepository) { it.organizations },
            VisualizationAuthorValidator(resourceRepository, statementRepository),
            VisualizationResourceCreator(resourceService),
            VisualizationDescriptionCreator(literalService, statementService),
            VisualizationAuthorCreator(resourceService, statementService, literalService, listService)
        )
        return steps.execute(command, VisualizationState()).visualizationId!!
    }

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
            visibility = visibility,
            unlistedBy = unlistedBy
        )
    }
}
