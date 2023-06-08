package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.PaperResourceWithPath
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LegacyRetrievePaperUseCase {
    fun findPapersRelatedToResource(related: ThingId, pageable: Pageable): Page<PaperResourceWithPath>
}
