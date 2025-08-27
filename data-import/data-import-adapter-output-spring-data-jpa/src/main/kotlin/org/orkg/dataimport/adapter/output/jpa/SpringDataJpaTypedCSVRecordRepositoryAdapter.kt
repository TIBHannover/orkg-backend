package org.orkg.dataimport.adapter.output.jpa

import org.orkg.common.serializeToByteArray
import org.orkg.common.withDefaultSort
import org.orkg.dataimport.adapter.output.jpa.internal.PostgresTypedCSVRecordRepository
import org.orkg.dataimport.adapter.output.jpa.internal.TypedCSVRecordEntity
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.TypedCSVRecord
import org.orkg.dataimport.output.TypedCSVRecordRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
@TransactionalOnJPA
class SpringDataJpaTypedCSVRecordRepositoryAdapter(
    private val postgresRepository: PostgresTypedCSVRecordRepository,
) : TypedCSVRecordRepository {
    override fun <S : TypedCSVRecord> save(entity: S): S {
        postgresRepository.save(entity.toTypedCSVRecordEntity())
        return entity
    }

    override fun <S : TypedCSVRecord> saveAll(entities: Iterable<S>): Iterable<S> {
        postgresRepository.saveAll(entities.map { it.toTypedCSVRecordEntity() })
        return entities
    }

    override fun findById(id: UUID): Optional<TypedCSVRecord> =
        postgresRepository.findById(id).map { it.toTypedCSVRecord() }

    override fun existsById(id: UUID): Boolean =
        postgresRepository.existsById(id)

    override fun findAll(sort: Sort): Iterable<TypedCSVRecord> =
        postgresRepository.findAll(sort).map { it.toTypedCSVRecord() }

    override fun findAll(pageable: Pageable): Page<TypedCSVRecord> =
        postgresRepository.findAll(pageable.withDefaultSort { Sort.by("itemNumber") })
            .map { it.toTypedCSVRecord() }

    override fun findAll(): Iterable<TypedCSVRecord> =
        postgresRepository.findAll().map { it.toTypedCSVRecord() }

    override fun findAllById(ids: Iterable<UUID>): Iterable<TypedCSVRecord> =
        postgresRepository.findAllById(ids).map { it.toTypedCSVRecord() }

    override fun findAllByCSVID(csvId: CSVID, pageable: Pageable): Page<TypedCSVRecord> =
        postgresRepository.findAllByCsvId(csvId.value, pageable.withDefaultSort { Sort.by("itemNumber") })
            .map { it.toTypedCSVRecord() }

    override fun deleteById(id: UUID) =
        postgresRepository.deleteById(id)

    override fun delete(entity: TypedCSVRecord) =
        postgresRepository.deleteById(entity.id)

    override fun deleteAllById(ids: Iterable<UUID>) =
        postgresRepository.deleteAllById(ids)

    override fun deleteAll(entities: Iterable<TypedCSVRecord>) =
        postgresRepository.deleteAllById(entities.map { it.id })

    override fun deleteAll() =
        postgresRepository.deleteAll()

    override fun deleteAllByCSVID(csvId: CSVID) {
        postgresRepository.deleteAllByCsvId(csvId.value)
    }

    override fun count(): Long =
        postgresRepository.count()

    private fun TypedCSVRecord.toTypedCSVRecordEntity(): TypedCSVRecordEntity =
        postgresRepository.findById(id).orElseGet { TypedCSVRecordEntity() }.apply {
            id = this@toTypedCSVRecordEntity.id
            csvId = this@toTypedCSVRecordEntity.csvId.value
            itemNumber = this@toTypedCSVRecordEntity.itemNumber
            lineNumber = this@toTypedCSVRecordEntity.lineNumber
            values = ArrayList(this@toTypedCSVRecordEntity.values).serializeToByteArray()
        }
}
