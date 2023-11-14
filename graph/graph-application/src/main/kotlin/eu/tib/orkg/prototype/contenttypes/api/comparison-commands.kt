package eu.tib.orkg.prototype.contenttypes.api

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.statements.domain.model.ExtractionMethod
import eu.tib.orkg.prototype.statements.domain.model.ThingId

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

interface PublishComparisonUseCase {
    fun publish(id: ThingId, subject: String, description: String)
}
