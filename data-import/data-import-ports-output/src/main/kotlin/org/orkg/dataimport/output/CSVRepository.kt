package org.orkg.dataimport.output

import org.orkg.common.ContributorId
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSVID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional

interface CSVRepository {
    fun save(csv: CSV)

    fun findById(id: CSVID): Optional<CSV>

    fun findAllByCreatedBy(createdBy: ContributorId, pageable: Pageable): Page<CSV>

    fun existsByDataMD5(hash: String): Boolean

    fun deleteById(id: CSVID)

    fun deleteAll()

    fun count(): Long
}
