package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.ContributionInfo
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ContributionComparisonRepository {
    fun findContributionsDetailsById(ids: List<ThingId>, pageable: Pageable): Page<ContributionInfo>
}
