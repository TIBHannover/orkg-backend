package org.orkg.contenttypes.input

import org.orkg.common.ThingId
import org.orkg.graph.domain.PaperResourceWithPath
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LegacyPaperUseCases : LegacyRetrievePaperUseCase

interface LegacyRetrievePaperUseCase {
    fun findAllPapersRelatedToResource(related: ThingId, pageable: Pageable): Page<PaperResourceWithPath>
}
