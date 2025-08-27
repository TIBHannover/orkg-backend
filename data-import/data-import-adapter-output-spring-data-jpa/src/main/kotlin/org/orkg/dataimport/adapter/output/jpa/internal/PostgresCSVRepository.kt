package org.orkg.dataimport.adapter.output.jpa.internal

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PostgresCSVRepository : JpaRepository<CSVEntity, UUID> {
    fun existsByDataMd5(hash: String): Boolean

    fun findAllByCreatedBy(createdBy: UUID, pageable: Pageable): Page<CSVEntity>
}
