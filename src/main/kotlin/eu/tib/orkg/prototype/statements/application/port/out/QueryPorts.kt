package eu.tib.orkg.prototype.statements.application.port.out

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LoadResourcePort {
    fun loadVerifiedResources(pageable: Pageable): Page<Resource>

    fun loadUnverifiedResources(pageable: Pageable): Page<Resource>
}

interface LoadPaperPort {
    fun loadVerifiedPapers(pageable: Pageable): Page<Resource>
    fun loadUnverifiedPapers(pageable: Pageable): Page<Resource>
}

interface GetPaperVerifiedFlagQuery {
    fun getPaperVerifiedFlag(id: ResourceId): Boolean?
}

interface LoadFeaturedResourcePort {
    fun loadFeaturedResources(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedResources(pageable: Pageable): Page<Resource>
}

interface LoadFeaturedPaperAdapter {
    fun loadFeaturedPapers(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedPapers(pageable: Pageable): Page<Resource>
}

interface GetFeaturedPaperFlagQuery {
    fun getFeaturedPaperFlag(id: ResourceId): Boolean?
}

interface GetFeaturedResourceFlagQuery {
    fun getFeaturedResourceFlag(id: ResourceId): Boolean? // Can combine this with the above
}
