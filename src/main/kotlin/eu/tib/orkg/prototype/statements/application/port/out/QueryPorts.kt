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
