package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ContributionRepository {
    fun findContributionByResourceId(id: ResourceId): Optional<Resource>
    fun findAllFeaturedContributions(pageable: Pageable): Page<Resource>
    fun findAllNonFeaturedContributions(pageable: Pageable): Page<Resource>
    fun findAllUnlistedContributions(pageable: Pageable): Page<Resource>
    fun findAllListedContributions(pageable: Pageable): Page<Resource>
}
