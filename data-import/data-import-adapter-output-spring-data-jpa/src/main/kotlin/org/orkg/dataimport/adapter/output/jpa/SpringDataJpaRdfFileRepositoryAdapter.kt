package org.orkg.dataimport.adapter.output.jpa

import org.orkg.common.ContributorId
import org.orkg.common.md5
import org.orkg.common.withDefaultSort
import org.orkg.dataimport.adapter.output.jpa.internal.PostgresRdfFileRepository
import org.orkg.dataimport.adapter.output.jpa.internal.RdfFileEntity
import org.orkg.dataimport.domain.rdf.RdfFile
import org.orkg.dataimport.output.RdfFileRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
@TransactionalOnJPA
class SpringDataJpaRdfFileRepositoryAdapter(
    private val postgresRepository: PostgresRdfFileRepository,
) : RdfFileRepository {
    override fun save(rdfFile: RdfFile) {
        postgresRepository.save(rdfFile.toRdfFileEntity())
    }

    override fun findById(id: UUID): Optional<RdfFile> =
        postgresRepository.findById(id).map(RdfFileEntity::toRdfFile)

    override fun findAllByCreatedBy(createdBy: ContributorId, pageable: Pageable): Page<RdfFile> =
        postgresRepository.findAllByCreatedBy(createdBy.value, pageable.withDefaultSort { Sort.by("createdAt") })
            .map(RdfFileEntity::toRdfFile)

    override fun existsByDataMD5(hash: String): Boolean =
        postgresRepository.existsByDataMd5(hash)

    override fun deleteById(id: UUID) =
        postgresRepository.deleteById(id)

    override fun deleteAll() =
        postgresRepository.deleteAll()

    override fun count(): Long =
        postgresRepository.count()

    private fun RdfFile.toRdfFileEntity(): RdfFileEntity =
        postgresRepository.findById(id).orElseGet { RdfFileEntity() }.apply {
            id = this@toRdfFileEntity.id
            name = this@toRdfFileEntity.name
            baseUri = this@toRdfFileEntity.baseUri?.toString()
            format = this@toRdfFileEntity.format
            state = this@toRdfFileEntity.state
            validationJobId = this@toRdfFileEntity.validationJobId?.value?.toString()
            importJobId = this@toRdfFileEntity.importJobId?.value?.toString()
            data = this@toRdfFileEntity.data
            dataMd5 = this@toRdfFileEntity.data.md5
            createdBy = this@toRdfFileEntity.createdBy.value
            createdAt = this@toRdfFileEntity.createdAt
            createdAtOffsetTotalSeconds = this@toRdfFileEntity.createdAt.offset.totalSeconds
        }
}
