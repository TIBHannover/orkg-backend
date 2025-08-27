package org.orkg.dataimport.adapter.output.jpa

import org.orkg.common.serializeToByteArray
import org.orkg.common.withDefaultSort
import org.orkg.dataimport.adapter.output.jpa.internal.PaperCSVRecordEntity
import org.orkg.dataimport.adapter.output.jpa.internal.PostgresPaperCSVRecordRepository
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecord
import org.orkg.dataimport.output.PaperCSVRecordRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
@TransactionalOnJPA
class SpringDataJpaPaperCSVRecordRepositoryAdapter(
    private val postgresRepository: PostgresPaperCSVRecordRepository,
) : PaperCSVRecordRepository {
    override fun <S : PaperCSVRecord> save(entity: S): S {
        postgresRepository.save(entity.toPaperCSVRecordEntity())
        return entity
    }

    override fun <S : PaperCSVRecord> saveAll(entities: Iterable<S>): Iterable<S> {
        postgresRepository.saveAll(entities.map { it.toPaperCSVRecordEntity() })
        return entities
    }

    override fun findById(id: UUID): Optional<PaperCSVRecord> =
        postgresRepository.findById(id).map { it.toPaperCSVRecord() }

    override fun findAll(): Iterable<PaperCSVRecord> =
        postgresRepository.findAll().map { it.toPaperCSVRecord() }

    override fun findAll(sort: Sort): Iterable<PaperCSVRecord> =
        postgresRepository.findAll(sort).map { it.toPaperCSVRecord() }

    override fun findAll(pageable: Pageable): Page<PaperCSVRecord> =
        postgresRepository.findAll(pageable.withDefaultSort { Sort.by("itemNumber") })
            .map { it.toPaperCSVRecord() }

    override fun findAllById(ids: Iterable<UUID>): Iterable<PaperCSVRecord> =
        postgresRepository.findAllById(ids).map { it.toPaperCSVRecord() }

    override fun findAllByCSVID(csvId: CSVID, pageable: Pageable): Page<PaperCSVRecord> =
        postgresRepository.findAllByCsvId(csvId.value, pageable.withDefaultSort { Sort.by("itemNumber") })
            .map { it.toPaperCSVRecord() }

    override fun existsById(id: UUID): Boolean =
        postgresRepository.existsById(id)

    override fun delete(entity: PaperCSVRecord) =
        postgresRepository.deleteById(entity.id)

    override fun deleteById(id: UUID) =
        postgresRepository.deleteById(id)

    override fun deleteAll() =
        postgresRepository.deleteAll()

    override fun deleteAll(entities: Iterable<PaperCSVRecord>) =
        postgresRepository.deleteAllById(entities.map { it.id })

    override fun deleteAllById(ids: Iterable<UUID>) =
        postgresRepository.deleteAllById(ids)

    override fun deleteAllByCSVID(csvId: CSVID) {
        postgresRepository.deleteAllByCsvId(csvId.value)
    }

    override fun count(): Long =
        postgresRepository.count()

    private fun PaperCSVRecord.toPaperCSVRecordEntity(): PaperCSVRecordEntity =
        postgresRepository.findById(id).orElseGet { PaperCSVRecordEntity() }.apply {
            id = this@toPaperCSVRecordEntity.id
            csvId = this@toPaperCSVRecordEntity.csvId.value
            itemNumber = this@toPaperCSVRecordEntity.itemNumber
            lineNumber = this@toPaperCSVRecordEntity.lineNumber
            title = this@toPaperCSVRecordEntity.title
            authors = ArrayList(this@toPaperCSVRecordEntity.authors).serializeToByteArray()
            publicationMonth = this@toPaperCSVRecordEntity.publicationMonth
            publicationYear = this@toPaperCSVRecordEntity.publicationYear
            publishedIn = this@toPaperCSVRecordEntity.publishedIn
            url = this@toPaperCSVRecordEntity.url?.toString()
            doi = this@toPaperCSVRecordEntity.doi
            researchFieldId = this@toPaperCSVRecordEntity.researchFieldId.value
            extractionMethod = this@toPaperCSVRecordEntity.extractionMethod
            statements = LinkedHashSet(this@toPaperCSVRecordEntity.statements).serializeToByteArray()
        }
}
