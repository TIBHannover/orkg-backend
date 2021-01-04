package eu.tib.orkg.prototype.statements.application.port.`in`

import eu.tib.orkg.prototype.statements.domain.model.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetVerifiedResourcesQuery {

    fun getVerifiedResources(pageable: Pageable): Page<Resource>

    fun getUnverifiedResources(pageable: Pageable): Page<Resource>
}

interface GetVerifiedPapersQuery {
    fun getVerifiedPapers(pageable: Pageable): Page<Resource>
    fun getUnverifiedPapers(pageable: Pageable): Page<Resource>
}
