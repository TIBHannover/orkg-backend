package org.orkg.dataimport.output

import org.orkg.dataimport.domain.rdf.RdfClass
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.UUID

interface RdfClassRepository :
    CrudRepository<RdfClass, UUID>,
    PagingAndSortingRepository<RdfClass, UUID> {
    fun findAllByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<RdfClass>

    fun deleteAllByRdfFileId(rdfFileId: UUID)
}
