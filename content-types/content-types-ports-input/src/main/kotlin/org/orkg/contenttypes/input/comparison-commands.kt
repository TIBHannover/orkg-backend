package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.graph.domain.ExtractionMethod

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
        val references: List<String>,
        val observatories: List<ObservatoryId>,
        val organizations: List<OrganizationId>,
        val isAnonymized: Boolean,
        val extractionMethod: ExtractionMethod
    )

    data class CreateComparisonRelatedResourceCommand(
        val comparisonId: ThingId,
        val contributorId: ContributorId,
        val label: String,
        val image: String?,
        val url: String?,
        val description: String?
    )

    data class CreateComparisonRelatedFigureCommand(
        val comparisonId: ThingId,
        val contributorId: ContributorId,
        val label: String,
        val image: String?,
        val description: String?
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
        val references: List<String>?,
        val observatories: List<ObservatoryId>?,
        val organizations: List<OrganizationId>?,
        val isAnonymized: Boolean?,
        val extractionMethod: ExtractionMethod?
    )

    data class UpdateComparisonRelatedResourceCommand(
        val comparisonId: ThingId,
        val comparisonRelatedResourceId: ThingId,
        val contributorId: ContributorId,
        val label: String?,
        val image: String?,
        val url: String?,
        val description: String?
    )

    data class UpdateComparisonRelatedFigureCommand(
        val comparisonId: ThingId,
        val comparisonRelatedFigureId: ThingId,
        val contributorId: ContributorId,
        val label: String?,
        val image: String?,
        val description: String?
    )
}

interface DeleteComparisonUseCase {
    fun deleteComparisonRelatedResource(
        comparisonId: ThingId,
        comparisonRelatedResourceId: ThingId,
        contributorId: ContributorId
    )
}

interface PublishComparisonUseCase {
    fun publish(command: PublishCommand)

    data class PublishCommand(
        val id: ThingId,
        val contributorId: ContributorId,
        val subject: String,
        val description: String,
        val authors: List<Author>
    )
}
