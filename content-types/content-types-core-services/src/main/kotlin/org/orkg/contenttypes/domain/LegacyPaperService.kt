package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.LegacyPaperUseCases
import org.orkg.contenttypes.output.PaperRepository
import org.orkg.graph.domain.PaperResourceWithPath
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class LegacyPaperService(
    private val repository: PaperRepository,
) : LegacyPaperUseCases {
    override fun findAllPapersRelatedToResource(related: ThingId, pageable: Pageable): Page<PaperResourceWithPath> =
        repository.findAllPapersRelatedToResource(related, pageable)
}
