package org.orkg.contenttypes.input

import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ResearchProblem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LegacyResearchProblemUseCases : LegacyRetrieveResearchProblemUseCase

interface LegacyRetrieveResearchProblemUseCase {
    fun findAllByDatasetId(id: ThingId, pageable: Pageable): Page<ResearchProblem>
}
