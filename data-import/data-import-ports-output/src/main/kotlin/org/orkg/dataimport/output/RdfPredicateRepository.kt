package org.orkg.dataimport.output

import org.orkg.dataimport.domain.rdf.RdfPredicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.UUID

interface RdfPredicateRepository :
    CrudRepository<RdfPredicate, UUID>,
    PagingAndSortingRepository<RdfPredicate, UUID> {
    fun findAllByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<RdfPredicate>

    fun deleteAllByRdfFileId(rdfFileId: UUID)
}
