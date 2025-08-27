package org.orkg.dataimport.adapter.output.jpa.internal

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PostgresPaperCSVRecordImportResultRepository : JpaRepository<PaperCSVRecordImportResultEntity, UUID> {
    fun findAllByCsvId(csvId: UUID, pageable: Pageable): Page<PaperCSVRecordImportResultEntity>

    fun deleteAllByCsvId(csvId: UUID)
}
