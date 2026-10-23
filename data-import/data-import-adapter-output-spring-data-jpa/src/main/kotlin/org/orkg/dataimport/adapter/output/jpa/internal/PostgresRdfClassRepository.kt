package org.orkg.dataimport.adapter.output.jpa.internal

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PostgresRdfClassRepository : JpaRepository<RdfClassEntity, UUID> {
    fun findAllByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<RdfClassEntity>

    fun deleteAllByRdfFileId(rdfFileId: UUID)
}
