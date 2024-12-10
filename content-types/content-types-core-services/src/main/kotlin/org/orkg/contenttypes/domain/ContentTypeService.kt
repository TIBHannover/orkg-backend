package org.orkg.contenttypes.domain

import java.time.OffsetDateTime
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ContentTypeUseCases
import org.orkg.contenttypes.output.ContentTypeRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ContentTypeService(
    private val repository: ContentTypeRepository,
    private val paperService: PaperService,
    private val comparisonService: ComparisonService,
    private val visualizationService: VisualizationService,
    private val templateService: TemplateService,
    private val literatureListService: LiteratureListService,
    private val smartReviewService: SmartReviewService
) : ContentTypeUseCases {
    override fun findAll(
        pageable: Pageable,
        classes: Set<ContentTypeClass>,
        visibility: VisibilityFilter?,
        createdBy: ContributorId?,
        createdAtStart: OffsetDateTime?,
        createdAtEnd: OffsetDateTime?,
        observatoryId: ObservatoryId?,
        organizationId: OrganizationId?,
        researchField: ThingId?,
        includeSubfields: Boolean,
        sustainableDevelopmentGoal: ThingId?,
        authorId: ThingId?,
    ): Page<ContentType> =
        repository.findAll(
            pageable = pageable,
            classes = classes,
            visibility = visibility,
            createdBy = createdBy,
            createdAtStart = createdAtStart,
            createdAtEnd = createdAtEnd,
            observatoryId = observatoryId,
            organizationId = organizationId,
            researchField = researchField,
            includeSubfields = includeSubfields,
            sustainableDevelopmentGoal = sustainableDevelopmentGoal,
            authorId = authorId,
        ).pmap { it.toContentType() }

    internal fun Resource.toContentType(): ContentType =
        when {
            Classes.paper in classes -> paperService.run { toPaper() }
            Classes.comparison in classes || Classes.comparisonPublished in classes -> comparisonService.run { toComparison() }
            Classes.visualization in classes -> visualizationService.run { toVisualization() }
            Classes.nodeShape in classes -> templateService.run { toTemplate() }
            Classes.literatureList in classes || Classes.literatureListPublished in classes -> literatureListService.run { toLiteratureList() }
            Classes.smartReview in classes || Classes.smartReviewPublished in classes -> smartReviewService.run { toSmartReview() }
            else -> throw IllegalArgumentException("""Cannot map resource "$id" to any content type.""")
        }
}
