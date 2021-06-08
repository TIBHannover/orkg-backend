package eu.tib.orkg.prototype.statements.application.service

import eu.tib.orkg.prototype.statements.application.port.`in`.GetFeaturedPapersQuery
import eu.tib.orkg.prototype.statements.application.port.`in`.GetFeaturedResourcesQuery
import eu.tib.orkg.prototype.statements.application.port.out.LoadFeaturedPaperAdapter
import eu.tib.orkg.prototype.statements.application.port.out.LoadFeaturedResourceAdapter
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class FeaturedResourcesService(
    private val loadFeaturedResourceAdapter: LoadFeaturedResourceAdapter,
    private val loadFeaturedPaperPort: LoadFeaturedPaperAdapter
) : GetFeaturedResourcesQuery, GetFeaturedPapersQuery {
    override fun getFeaturedResources(pageable: Pageable) =
        loadFeaturedResourceAdapter.loadFeaturedResources(pageable)

    override fun getNonFeaturedResources(pageable: Pageable) =
        loadFeaturedResourceAdapter.loadNonFeaturedResources(pageable)

    override fun getFeaturedPapers(pageable: Pageable) =
        loadFeaturedPaperPort.loadFeaturedPapers(pageable)

    override fun getNonFeaturedPapers(pageable: Pageable) =
        loadFeaturedPaperPort.loadNonFeaturedPapers(pageable)
}
