package org.orkg.contenttypes.input

import org.orkg.contenttypes.domain.ResearchField
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LegacyResearchFieldUseCases : LegacyRetrieveResearchFieldUseCase

interface LegacyRetrieveResearchFieldUseCase {
    fun findAllWithBenchmarks(pageable: Pageable): Page<ResearchField>
}
