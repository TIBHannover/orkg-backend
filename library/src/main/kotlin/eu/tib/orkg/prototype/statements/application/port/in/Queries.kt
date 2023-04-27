package eu.tib.orkg.prototype.statements.application.port.`in`

import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

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
        subjects: List<ThingId>,
        pageable: Pageable
    ): Map<String, Iterable<GeneralStatement>>
    fun getBulkStatementsByObjects(
        objects: List<ThingId>,
        pageable: Pageable
    ): Map<String, Iterable<GeneralStatement>>
}
