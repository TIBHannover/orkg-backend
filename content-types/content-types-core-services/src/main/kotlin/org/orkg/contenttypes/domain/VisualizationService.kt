package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.orkg.contenttypes.domain.actions.CreateVisualizationCommand
import org.orkg.contenttypes.domain.actions.LabelValidator
import org.orkg.contenttypes.domain.actions.ObservatoryValidator
import org.orkg.contenttypes.domain.actions.OrganizationValidator
import org.orkg.contenttypes.domain.actions.VisualizationState
import org.orkg.contenttypes.domain.actions.execute
import org.orkg.contenttypes.domain.actions.visualizations.VisualizationAuthorCreator
import org.orkg.contenttypes.domain.actions.visualizations.VisualizationAuthorValidator
import org.orkg.contenttypes.domain.actions.visualizations.VisualizationDescriptionCreator
import org.orkg.contenttypes.domain.actions.visualizations.VisualizationResourceCreator
import org.orkg.contenttypes.input.VisualizationUseCases
import org.orkg.contenttypes.output.VisualizationRepository
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class VisualizationService(
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationRepository: OrganizationRepository,
    private val unsafeResourceUseCases: UnsafeResourceUseCases,
    private val statementService: StatementUseCases,
    private val literalService: LiteralUseCases,
    private val listService: ListUseCases,
    private val visualizationRepository: VisualizationRepository
) : VisualizationUseCases {
    override fun findById(id: ThingId): Optional<Visualization> =
        resourceRepository.findById(id)
            .filter { it is Resource && Classes.visualization in it.classes }
            .map { it.toVisualization() }

    override fun findAll(
        label: SearchString?,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        pageable: Pageable
    ): Page<Visualization> =
        visualizationRepository.findAll(
            label = label,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            pageable = pageable
        ).map { it.toVisualization() }

    override fun create(command: CreateVisualizationCommand): ThingId {
        val steps = listOf(
            LabelValidator { it.title },
            ObservatoryValidator(observatoryRepository, { it.observatories }),
            OrganizationValidator(organizationRepository, { it.organizations }),
            VisualizationAuthorValidator(resourceRepository, statementRepository),
            VisualizationResourceCreator(unsafeResourceUseCases),
            VisualizationDescriptionCreator(literalService, statementService),
            VisualizationAuthorCreator(unsafeResourceUseCases, statementService, literalService, listService)
        )
        return steps.execute(command, VisualizationState()).visualizationId!!
    }

    internal fun findSubgraph(resource: Resource): ContentTypeSubgraph {
        val statements = statementRepository.fetchAsBundle(
            id = resource.id,
            configuration = BundleConfiguration(
                minLevel = null,
                maxLevel = 3,
                blacklist = listOf(Classes.researchField),
                whitelist = emptyList()
            ),
            sort = Sort.unsorted()
        ).groupBy { it.subject.id }
        return ContentTypeSubgraph(resource.id, statements)
    }

    internal fun Resource.toVisualization(): Visualization =
        findSubgraph(this).let { Visualization.from(this, it.statements) }
}
