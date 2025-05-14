package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonRelatedFigure
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface ComparisonRelatedFigureUseCases :
    RetrieveComparisonRelatedFigureUseCase,
    CreateComparisonRelatedFigureUseCase,
    UpdateComparisonRelatedFigureUseCase,
    DeleteComparisonRelatedFigureUseCase

interface RetrieveComparisonRelatedFigureUseCase {
    fun findByIdAndComparisonId(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedFigure>

    fun findAllByComparisonId(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedFigure>
}

interface CreateComparisonRelatedFigureUseCase {
    fun create(command: CreateCommand): ThingId

    data class CreateCommand(
        val comparisonId: ThingId,
        val contributorId: ContributorId,
        val label: String,
        val image: String?,
        val description: String?,
    )
}

interface UpdateComparisonRelatedFigureUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val comparisonId: ThingId,
        val comparisonRelatedFigureId: ThingId,
        val contributorId: ContributorId,
        val label: String?,
        val image: String?,
        val description: String?,
    )
}

interface DeleteComparisonRelatedFigureUseCase {
    fun delete(
        comparisonId: ThingId,
        comparisonRelatedFigureId: ThingId,
        contributorId: ContributorId,
    )
}
