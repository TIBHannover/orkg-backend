package org.orkg.dataimport.adapter.output.jpa

import org.orkg.common.withDefaultSort
import org.orkg.dataimport.adapter.output.jpa.internal.PaperCSVRecordImportResultEntity
import org.orkg.dataimport.adapter.output.jpa.internal.PostgresPaperCSVRecordImportResultRepository
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordImportResult
import org.orkg.dataimport.output.PaperCSVRecordImportResultRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
@TransactionalOnJPA
class SpringDataJpaPaperCSVRecordImportResultRepositoryAdapter(
    private val postgresRepository: PostgresPaperCSVRecordImportResultRepository,
) : PaperCSVRecordImportResultRepository {
    override fun <S : PaperCSVRecordImportResult> save(entity: S): S {
        postgresRepository.save(entity.toPaperCSVRecordImportResultEntity())
        return entity
    }

    override fun <S : PaperCSVRecordImportResult> saveAll(entities: Iterable<S>): Iterable<S> {
        postgresRepository.saveAll(entities.map { it.toPaperCSVRecordImportResultEntity() })
        return entities
    }

    override fun findById(id: UUID): Optional<PaperCSVRecordImportResult> =
        postgresRepository.findById(id).map { it.toPaperCSVRecordImportResult() }

    override fun findAll(): Iterable<PaperCSVRecordImportResult> =
        postgresRepository.findAll().map { it.toPaperCSVRecordImportResult() }

    override fun findAllById(ids: Iterable<UUID>): Iterable<PaperCSVRecordImportResult> =
        postgresRepository.findAllById(ids).map { it.toPaperCSVRecordImportResult() }

    override fun findAllByCSVID(csvId: CSVID, pageable: Pageable): Page<PaperCSVRecordImportResult> =
        postgresRepository.findAllByCsvId(csvId.value, pageable.withDefaultSort { Sort.by("itemNumber") })
            .map { it.toPaperCSVRecordImportResult() }

    override fun existsById(id: UUID): Boolean =
        postgresRepository.existsById(id)

    override fun delete(entity: PaperCSVRecordImportResult) =
        postgresRepository.deleteById(entity.id)

    override fun deleteById(id: UUID) =
        postgresRepository.deleteById(id)

    override fun deleteAll(entities: Iterable<PaperCSVRecordImportResult>) =
        postgresRepository.deleteAllById(entities.map { it.id })

    override fun deleteAllById(ids: Iterable<UUID>) =
        postgresRepository.deleteAllById(ids)

    override fun deleteAll() =
        postgresRepository.deleteAll()

    override fun deleteAllByCSVID(csvId: CSVID) {
        postgresRepository.deleteAllByCsvId(csvId.value)
    }

    override fun count(): Long =
        postgresRepository.count()

    private fun PaperCSVRecordImportResult.toPaperCSVRecordImportResultEntity(): PaperCSVRecordImportResultEntity =
        postgresRepository.findById(id).orElseGet { PaperCSVRecordImportResultEntity() }.apply {
            id = this@toPaperCSVRecordImportResultEntity.id
            importedEntityId = this@toPaperCSVRecordImportResultEntity.importedEntityId.value
            importedEntityType = this@toPaperCSVRecordImportResultEntity.importedEntityType
            csvId = this@toPaperCSVRecordImportResultEntity.csvId.value
            itemNumber = this@toPaperCSVRecordImportResultEntity.itemNumber
            lineNumber = this@toPaperCSVRecordImportResultEntity.lineNumber
        }
}
