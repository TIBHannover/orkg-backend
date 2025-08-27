package org.orkg.dataimport.output

import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.TypedCSVRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.UUID

interface TypedCSVRecordRepository :
    PagingAndSortingRepository<TypedCSVRecord, UUID>,
    CrudRepository<TypedCSVRecord, UUID> {
    fun findAllByCSVID(csvId: CSVID, pageable: Pageable): Page<TypedCSVRecord>

    fun deleteAllByCSVID(csvId: CSVID)
}
