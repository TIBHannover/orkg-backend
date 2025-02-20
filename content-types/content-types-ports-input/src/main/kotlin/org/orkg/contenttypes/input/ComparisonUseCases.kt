package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.Comparison
import org.orkg.contenttypes.domain.ComparisonConfig
import org.orkg.contenttypes.domain.ComparisonData
import org.orkg.contenttypes.domain.ComparisonRelatedFigure
import org.orkg.contenttypes.domain.ComparisonRelatedResource
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime
import java.util.Optional

interface ComparisonUseCases :
    RetrieveComparisonUseCase,
    CreateComparisonUseCase,
    UpdateComparisonUseCase,
    DeleteComparisonUseCase,
    PublishComparisonUseCase

interface RetrieveComparisonUseCase {
    fun findAll(
        pageable: Pageable,
        label: SearchString? = null,
        doi: String? = null,
        visibility: VisibilityFilter? = null,
        createdBy: ContributorId? = null,
        createdAtStart: OffsetDateTime? = null,
        createdAtEnd: OffsetDateTime? = null,
        observatoryId: ObservatoryId? = null,
        organizationId: OrganizationId? = null,
        researchField: ThingId? = null,
        includeSubfields: Boolean = false,
        published: Boolean? = null,
        sustainableDevelopmentGoal: ThingId? = null,
        researchProblem: ThingId?,
    ): Page<Comparison>

    fun findById(id: ThingId): Optional<Comparison>

    fun findRelatedResourceById(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedResource>

    fun findAllRelatedResourcesById(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedResource>

    fun findRelatedFigureById(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedFigure>

    fun findAllRelatedFiguresById(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedFigure>

    // An unpublished comparison is a comparison that does not have a DOI and is not a draft comparison (ComparisonDraft)
    fun findAllCurrentAndListedAndUnpublishedComparisons(pageable: Pageable): Page<Comparison>
}

interface CreateComparisonUseCase {
    fun create(command: CreateCommand): ThingId

    fun createComparisonRelatedResource(command: CreateComparisonRelatedResourceCommand): ThingId

    fun createComparisonRelatedFigure(command: CreateComparisonRelatedFigureCommand): ThingId

    data class CreateCommand(
        val contributorId: ContributorId,
        val title: String,
        val description: String,
        val researchFields: List<ThingId>,
        val authors: List<Author>,
        val sustainableDevelopmentGoals: Set<ThingId>,
        val contributions: List<ThingId>,
        val config: ComparisonConfig,
        val data: ComparisonData,
        val references: List<String>,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>,
        val isAnonymized: Boolean,
        val extractionMethod: ExtractionMethod,
    )

    data class CreateComparisonRelatedResourceCommand(
        val comparisonId: ThingId,
        val contributorId: ContributorId,
        val label: String,
        val image: String?,
        val url: String?,
        val description: String?,
    )

    data class CreateComparisonRelatedFigureCommand(
        val comparisonId: ThingId,
        val contributorId: ContributorId,
        val label: String,
        val image: String?,
        val description: String?,
    )
}

interface UpdateComparisonUseCase {
    fun update(command: UpdateCommand)

    fun updateComparisonRelatedResource(command: UpdateComparisonRelatedResourceCommand)

    fun updateComparisonRelatedFigure(command: UpdateComparisonRelatedFigureCommand)

    data class UpdateCommand(
        val comparisonId: ThingId,
        val contributorId: ContributorId,
        val title: String?,
        val description: String?,
        val researchFields: List<ThingId>?,
        val authors: List<Author>?,
        val sustainableDevelopmentGoals: Set<ThingId>?,
        val contributions: List<ThingId>?,
        val config: ComparisonConfig?,
        val data: ComparisonData?,
        val references: List<String>?,
        val observatories: List<ObservatoryId>?,
        val organizations: List<OrganizationId>?,
        val isAnonymized: Boolean?,
        val extractionMethod: ExtractionMethod?,
        val visibility: Visibility?,
    )

    data class UpdateComparisonRelatedResourceCommand(
        val comparisonId: ThingId,
        val comparisonRelatedResourceId: ThingId,
        val contributorId: ContributorId,
        val label: String?,
        val image: String?,
        val url: String?,
        val description: String?,
    )

    data class UpdateComparisonRelatedFigureCommand(
        val comparisonId: ThingId,
        val comparisonRelatedFigureId: ThingId,
        val contributorId: ContributorId,
        val label: String?,
        val image: String?,
        val description: String?,
    )
}

interface DeleteComparisonUseCase {
    fun deleteComparisonRelatedResource(
        comparisonId: ThingId,
        comparisonRelatedResourceId: ThingId,
        contributorId: ContributorId,
    )

    fun deleteComparisonRelatedFigure(
        comparisonId: ThingId,
        comparisonRelatedFigureId: ThingId,
        contributorId: ContributorId,
    )
}

interface PublishComparisonUseCase {
    fun publish(command: PublishCommand): ThingId

    data class PublishCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val subject: String,
        val description: String,
        val authors: List<Author>,
        val assignDOI: Boolean,
    )
}
