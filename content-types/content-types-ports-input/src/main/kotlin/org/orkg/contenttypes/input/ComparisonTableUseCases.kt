package org.orkg.contenttypes.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.LabeledComparisonPath
import org.orkg.contenttypes.domain.SimpleComparisonPath
import java.util.Optional

interface ComparisonTableUseCases :
    RetrieveComparisonTablePathsUseCase,
    RetrieveComparisonTableUseCase,
    UpdateComparisonTableUseCase

interface RetrieveComparisonTablePathsUseCase {
    fun findAllPathsByComparisonId(comparisonId: ThingId): List<LabeledComparisonPath>
}

interface RetrieveComparisonTableUseCase {
    fun findByComparisonId(comparisonId: ThingId): Optional<ComparisonTable>
}

interface UpdateComparisonTableUseCase {
    fun update(command: UpdateCommand)

    data class UpdateCommand(
        val comparisonId: ThingId,
        val contributorId: ContributorId,
        val selectedPaths: List<SimpleComparisonPath>,
    )
}
