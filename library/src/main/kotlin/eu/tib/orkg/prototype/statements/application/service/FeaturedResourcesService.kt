/*package eu.tib.orkg.prototype.statements.application.service

import eu.tib.orkg.prototype.statements.application.port.`in`.GetPapersQuery
import eu.tib.orkg.prototype.statements.application.port.`in`.GetResourcesQuery
import eu.tib.orkg.prototype.statements.application.port.out.LoadPaperAdapter
import eu.tib.orkg.prototype.statements.application.port.out.LoadResourceAdapter
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class FeaturedResourcesService(
    private val loadResourceAdapter: LoadResourceAdapter,
    private val loadPaperAdapter: LoadPaperAdapter
) : GetResourcesQuery, GetPapersQuery {
    override fun getFeaturedResources(pageable: Pageable) =
        loadResourceAdapter.loadFeaturedResources(pageable)

    override fun getNonFeaturedResources(pageable: Pageable) =
        loadResourceAdapter.loadNonFeaturedResources(pageable)

    override fun getFeaturedPapers(pageable: Pageable) =
        loadPaperAdapter.loadFeaturedPapers(pageable)

    override fun getNonFeaturedPapers(pageable: Pageable) =
        loadPaperAdapter.loadNonFeaturedPapers(pageable)
}
*/
