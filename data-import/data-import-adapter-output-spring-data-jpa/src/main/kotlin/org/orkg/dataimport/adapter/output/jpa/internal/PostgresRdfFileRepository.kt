package org.orkg.dataimport.adapter.output.jpa.internal

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PostgresRdfFileRepository : JpaRepository<RdfFileEntity, UUID> {
    fun findAllByCreatedBy(createdBy: UUID, pageable: Pageable): Page<RdfFileEntity>

    fun existsByDataMd5(hash: String): Boolean
}
