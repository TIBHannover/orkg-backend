package eu.tib.orkg.prototype.statements.application.port.`in`

import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
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

interface GetFeaturedResourcesQuery {
    fun getFeaturedResources(pageable: Pageable): Page<Resource>
    fun getNonFeaturedResources(pageable: Pageable): Page<Resource>
}

interface GetFeaturedPapersQuery {
    fun getFeaturedPapers(pageable: Pageable): Page<Resource>
    fun getNonFeaturedPapers(pageable: Pageable): Page<Resource>
}
