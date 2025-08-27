package org.orkg.dataimport.adapter.output.jpa

import org.orkg.common.ContributorId
import org.orkg.common.md5
import org.orkg.common.withDefaultSort
import org.orkg.dataimport.adapter.output.jpa.internal.CSVEntity
import org.orkg.dataimport.adapter.output.jpa.internal.PostgresCSVRepository
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.output.CSVRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.Optional

@Component
@TransactionalOnJPA
class SpringDataJpaCSVRepositoryAdapter(
    private val postgresRepository: PostgresCSVRepository,
) : CSVRepository {
    override fun save(csv: CSV) {
        postgresRepository.save(csv.toCSVEntity())
    }

    override fun findById(id: CSVID): Optional<CSV> =
        postgresRepository.findById(id.value).map(CSVEntity::toCSV)

    override fun findAllByCreatedBy(createdBy: ContributorId, pageable: Pageable): Page<CSV> =
        postgresRepository.findAllByCreatedBy(createdBy.value, pageable.withDefaultSort { Sort.by("createdAt") })
            .map(CSVEntity::toCSV)

    override fun existsByDataMD5(hash: String): Boolean =
        postgresRepository.existsByDataMd5(hash)

    override fun deleteById(id: CSVID) =
        postgresRepository.deleteById(id.value)

    override fun deleteAll() =
        postgresRepository.deleteAll()

    override fun count(): Long =
        postgresRepository.count()

    private fun CSV.toCSVEntity(): CSVEntity =
        postgresRepository.findById(id.value).orElseGet { CSVEntity() }.apply {
            id = this@toCSVEntity.id.value
            name = this@toCSVEntity.name
            type = this@toCSVEntity.type
            format = this@toCSVEntity.format
            state = this@toCSVEntity.state
            validationJobId = this@toCSVEntity.validationJobId?.value?.toString()
            importJobId = this@toCSVEntity.importJobId?.value?.toString()
            data = this@toCSVEntity.data
            dataMd5 = this@toCSVEntity.data.md5
            createdBy = this@toCSVEntity.createdBy.value
            createdAt = this@toCSVEntity.createdAt
            createdAtOffsetTotalSeconds = this@toCSVEntity.createdAt.offset.totalSeconds
        }
}
