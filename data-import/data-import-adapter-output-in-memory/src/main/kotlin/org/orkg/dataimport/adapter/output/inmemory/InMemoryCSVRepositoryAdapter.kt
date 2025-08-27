package org.orkg.dataimport.adapter.output.inmemory

import org.orkg.common.ContributorId
import org.orkg.common.md5
import org.orkg.common.paged
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.output.CSVRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class InMemoryCSVRepositoryAdapter : CSVRepository {
    private val data = mutableMapOf<CSVID, CSV>()

    override fun save(csv: CSV) {
        data[csv.id] = csv
    }

    override fun findById(id: CSVID): Optional<CSV> =
        Optional.ofNullable(data[id])

    override fun findAllByCreatedBy(createdBy: ContributorId, pageable: Pageable): Page<CSV> =
        data.values.filter { it.createdBy == createdBy }
            .sortedBy { it.createdAt }
            .paged(pageable)

    override fun existsByDataMD5(hash: String): Boolean =
        data.values.any { it.data.md5 == hash }

    override fun deleteById(id: CSVID) {
        data.remove(id)
    }

    override fun deleteAll() = data.clear()

    override fun count(): Long = data.size.toLong()
}
