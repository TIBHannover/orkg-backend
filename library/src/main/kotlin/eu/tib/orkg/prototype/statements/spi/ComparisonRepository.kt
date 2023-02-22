package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ComparisonRepository {
    fun findComparisonByResourceId(id: ThingId): Optional<Resource>
    fun findAllFeaturedComparisons(pageable: Pageable): Page<Resource>
    fun findAllNonFeaturedComparisons(pageable: Pageable): Page<Resource>
    fun findAllUnlistedComparisons(pageable: Pageable): Page<Resource>
    fun findAllListedComparisons(pageable: Pageable): Page<Resource>
}
