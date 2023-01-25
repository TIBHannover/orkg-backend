package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface VisualizationRepository {
    fun findVisualizationByResourceId(id: ResourceId): Optional<Resource>
    fun findAllFeaturedVisualizations(pageable: Pageable): Page<Resource>
    fun findAllNonFeaturedVisualizations(pageable: Pageable): Page<Resource>
    fun findAllUnlistedVisualizations(pageable: Pageable): Page<Resource>
    fun findAllListedVisualizations(pageable: Pageable): Page<Resource>
}
