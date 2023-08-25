package eu.tib.orkg.prototype.contenttypes.api

import eu.tib.orkg.prototype.contenttypes.domain.model.Contribution
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveContributionUseCase {
    fun findById(id: ThingId): Contribution
    fun findAll(pageable: Pageable): Page<Contribution>
}
