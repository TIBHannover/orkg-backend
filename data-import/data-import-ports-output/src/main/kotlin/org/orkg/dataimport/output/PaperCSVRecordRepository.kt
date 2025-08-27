package org.orkg.dataimport.output

import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.UUID

interface PaperCSVRecordRepository :
    PagingAndSortingRepository<PaperCSVRecord, UUID>,
    CrudRepository<PaperCSVRecord, UUID> {
    fun findAllByCSVID(csvId: CSVID, pageable: Pageable): Page<PaperCSVRecord>

    fun deleteAllByCSVID(csvId: CSVID)
}
