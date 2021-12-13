package eu.tib.orkg.prototype.statements.application.service

import eu.tib.orkg.prototype.statements.application.port.`in`.GetPapersQuery
import eu.tib.orkg.prototype.statements.application.port.`in`.GetResourcesQuery
import eu.tib.orkg.prototype.statements.application.port.out.LoadPaperPort
import eu.tib.orkg.prototype.statements.application.port.out.LoadResourcePort
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

// FIXME: MarkVerifiedUseCase, MarkFeaturedService, MarkAsInlisted is currently implemented in ResourceService but should be extracted.
// FIXME: The LoadResourcePort is currently implemented in ResourceService but should be extracted.

@Service
class GetResourceFlagsService(
    private val loadResourcePort: LoadResourcePort,
    private val loadPaperPort: LoadPaperPort
) : GetResourcesQuery, GetPapersQuery {
    override fun getVerifiedResources(pageable: Pageable) =
        loadResourcePort.loadVerifiedResources(pageable)

    override fun getUnverifiedResources(pageable: Pageable) =
        loadResourcePort.loadUnverifiedResources(pageable)

    override fun getFeaturedResources(pageable: Pageable) =
        loadResourcePort.loadFeaturedResources(pageable)

    override fun getNonFeaturedResources(pageable: Pageable) =
        loadResourcePort.loadNonFeaturedResources(pageable)

    override fun getUnlistedResources(pageable: Pageable) =
        loadResourcePort.loadUnlistedResources(pageable)

    override fun getListedResources(pageable: Pageable) =
        loadResourcePort.loadListedResources(pageable)

    override fun getFeaturedPapers(pageable: Pageable) =
        loadPaperPort.loadFeaturedPapers(pageable)

    override fun getNonFeaturedPapers(pageable: Pageable) =
        loadPaperPort.loadNonFeaturedPapers(pageable)

    override fun getVerifiedPapers(pageable: Pageable) =
        loadPaperPort.loadVerifiedPapers(pageable)

    override fun getUnverifiedPapers(pageable: Pageable) =
        loadPaperPort.loadUnverifiedPapers(pageable)

    override fun getUnlistedPapers(pageable: Pageable) =
        loadPaperPort.loadUnlistedPapers(pageable)

    override fun getListedPapers(pageable: Pageable) =
        loadPaperPort.loadListedPapers(pageable)
}
