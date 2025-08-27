package org.orkg.dataimport.output

import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordImportResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface PaperCSVRecordImportResultRepository : CrudRepository<PaperCSVRecordImportResult, UUID> {
    fun findAllByCSVID(csvId: CSVID, pageable: Pageable): Page<PaperCSVRecordImportResult>

    fun deleteAllByCSVID(csvId: CSVID)
}
