package eu.tib.orkg.prototype.statements.application.service

import eu.tib.orkg.prototype.statements.application.port.`in`.GetVerifiedPapersQuery
import eu.tib.orkg.prototype.statements.application.port.`in`.GetVerifiedResourcesQuery
import eu.tib.orkg.prototype.statements.application.port.out.LoadPaperPort
import eu.tib.orkg.prototype.statements.application.port.out.LoadResourcePort
import eu.tib.orkg.prototype.statements.domain.model.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

// FIXME: The MarkVerifiedUseCase is currently implemented in ResourceService but should be extracted.
// FIXME: The LoadResourcePort is currently implemented in ResourceService but should be extracted.

@Service
class GetVerifiedResourcesService(
    private val loadResourcePort: LoadResourcePort,
    private val loadPaperPort: LoadPaperPort
) : GetVerifiedResourcesQuery, GetVerifiedPapersQuery {
    override fun getVerifiedResources(pageable: Pageable): Page<Resource> =
        loadResourcePort.loadVerifiedResources(pageable)

    override fun getUnverifiedResources(pageable: Pageable): Page<Resource> =
        loadResourcePort.loadUnverifiedResources(pageable)

    override fun getVerifiedPapers(pageable: Pageable): Page<Resource> =
        loadPaperPort.loadVerifiedPapers(pageable)

    override fun getUnverifiedPapers(pageable: Pageable): Page<Resource> =
        loadPaperPort.loadUnverifiedPapers(pageable)
}
