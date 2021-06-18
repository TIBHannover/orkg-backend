package eu.tib.orkg.prototype.statements.application.service

import eu.tib.orkg.prototype.statements.application.port.`in`.GetPapersQuery
import eu.tib.orkg.prototype.statements.application.port.`in`.GetResourcesQuery
import eu.tib.orkg.prototype.statements.application.port.out.LoadPaperAdapter
import eu.tib.orkg.prototype.statements.application.port.out.LoadResourceAdapter
import eu.tib.orkg.prototype.statements.domain.model.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

// FIXME: The MarkVerifiedUseCase is currently implemented in ResourceService but should be extracted.
// FIXME: The LoadResourcePort is currently implemented in ResourceService but should be extracted.

@Service
class GetResourcesService(
    private val loadResourceAdapter: LoadResourceAdapter,
    private val loadPaperAdapter: LoadPaperAdapter
) : GetResourcesQuery, GetPapersQuery {
    override fun getVerifiedResources(pageable: Pageable): Page<Resource> =
        loadResourceAdapter.loadVerifiedResources(pageable)

    override fun getUnverifiedResources(pageable: Pageable): Page<Resource> =
        loadResourceAdapter.loadUnverifiedResources(pageable)

    override fun getFeaturedResources(pageable: Pageable) =
        loadResourceAdapter.loadFeaturedResources(pageable)

    override fun getNonFeaturedResources(pageable: Pageable) =
        loadResourceAdapter.loadNonFeaturedResources(pageable)

    override fun getFeaturedPapers(pageable: Pageable) =
        loadPaperAdapter.loadFeaturedPapers(pageable)

    override fun getNonFeaturedPapers(pageable: Pageable) =
        loadPaperAdapter.loadNonFeaturedPapers(pageable)

    override fun getUnlistedResources(pageable: Pageable): Page<Resource> =
        loadResourceAdapter.loadUnlistedPapers(pageable)

    override fun getVerifiedPapers(pageable: Pageable): Page<Resource> =
        loadPaperAdapter.loadVerifiedPapers(pageable)

    override fun getUnverifiedPapers(pageable: Pageable): Page<Resource> =
        loadPaperAdapter.loadUnverifiedPapers(pageable)

    override fun getUnlistedPapers(pageable: Pageable): Page<Resource> =
        loadPaperAdapter.loadUnlistedPapers(pageable)

}

