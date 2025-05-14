package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonRelatedResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ComparisonRelatedResourceUseCases :
    RetrieveComparisonRelatedResourceUseCase,
    CreateComparisonRelatedResourceUseCase,
    UpdateComparisonRelatedResourceUseCase,
    DeleteComparisonRelatedResourceUseCase

interface RetrieveComparisonRelatedResourceUseCase {
    fun findByIdAndComparisonId(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedResource>

    fun findAllByComparisonId(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedResource>
}

interface CreateComparisonRelatedResourceUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val comparisonId: ThingId,
        val contributorId: ContributorId,
        val label: String,
        val image: String?,
        val url: String?,
        val description: String?,
    )
}

interface UpdateComparisonRelatedResourceUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val comparisonId: ThingId,
        val comparisonRelatedResourceId: ThingId,
        val contributorId: ContributorId,
        val label: String?,
        val image: String?,
        val url: String?,
        val description: String?,
    )
}

interface DeleteComparisonRelatedResourceUseCase {
    fun delete(
        comparisonId: ThingId,
        comparisonRelatedResourceId: ThingId,
        contributorId: ContributorId,
    )
}
