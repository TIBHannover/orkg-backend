package eu.tib.orkg.prototype.statements.application.port.`in`

import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetResourcesQuery {
    fun getVerifiedResources(pageable: Pageable): Page<Resource>
    fun getUnverifiedResources(pageable: Pageable): Page<Resource>
    fun getFeaturedResources(pageable: Pageable): Page<Resource>
    fun getNonFeaturedResources(pageable: Pageable): Page<Resource>
    fun getUnlistedResources(pageable: Pageable): Page<Resource>
    fun getListedResources(pageable: Pageable): Page<Resource>
}

interface GetPapersQuery {
    fun getVerifiedPapers(pageable: Pageable): Page<Resource>
    fun getUnverifiedPapers(pageable: Pageable): Page<Resource>
    fun getFeaturedPapers(pageable: Pageable): Page<Resource>
    fun getNonFeaturedPapers(pageable: Pageable): Page<Resource>
    fun getUnlistedPapers(pageable: Pageable): Page<Resource>
    fun getListedPapers(pageable: Pageable): Page<Resource>
}

interface GetBulkStatementsQuery {
    fun getBulkStatementsBySubjects(
        subjects: List<String>,
        pageable: Pageable
    ): Map<String, Iterable<GeneralStatement>>
    fun getBulkStatementsByObjects(
        objects: List<String>,
        pageable: Pageable
    ): Map<String, Iterable<GeneralStatement>>
}
