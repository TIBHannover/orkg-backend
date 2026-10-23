package org.orkg.dataimport.output

import org.orkg.dataimport.domain.rdf.RdfResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.UUID

interface RdfResourceRepository :
    CrudRepository<RdfResource, UUID>,
    PagingAndSortingRepository<RdfResource, UUID> {
    fun findAllByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<RdfResource>

    fun deleteAllByRdfFileId(rdfFileId: UUID)
}
