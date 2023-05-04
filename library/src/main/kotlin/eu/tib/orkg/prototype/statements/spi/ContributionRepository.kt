package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.contenttypes.domain.model.Visibility
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ContributionRepository {
    fun findContributionByResourceId(id: ThingId): Optional<Resource>
    fun findAllListedContributions(pageable: Pageable): Page<Resource>
    fun findAllContributionsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource>
}
