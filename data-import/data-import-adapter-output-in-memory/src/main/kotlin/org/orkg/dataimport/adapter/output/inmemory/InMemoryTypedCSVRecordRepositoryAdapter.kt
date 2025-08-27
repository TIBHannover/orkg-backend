package org.orkg.dataimport.adapter.output.inmemory

import org.orkg.common.paged
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.TypedCSVRecord
import org.orkg.dataimport.output.TypedCSVRecordRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class InMemoryTypedCSVRecordRepositoryAdapter : TypedCSVRecordRepository {
    private val data = mutableMapOf<UUID, TypedCSVRecord>()
    private val defaultComparator = Comparator.comparing<TypedCSVRecord, UUID> { it.csvId.value }
        .thenComparing(Comparator.comparing<TypedCSVRecord, Long> { it.itemNumber })

    override fun <S : TypedCSVRecord> save(entity: S): S {
        data[entity.id] = entity
        return entity
    }

    override fun <S : TypedCSVRecord> saveAll(entities: Iterable<S>): Iterable<S> =
        entities.map(::save)

    override fun findById(id: UUID): Optional<TypedCSVRecord> =
        Optional.ofNullable(data[id])

    override fun findAllById(ids: Iterable<UUID>): Iterable<TypedCSVRecord> =
        data.values.filter { it.id in ids }

    override fun findAll(): Iterable<TypedCSVRecord> =
        data.values

    override fun findAll(sort: Sort): Iterable<TypedCSVRecord> =
        findAll().sortedWith(defaultComparator)

    override fun findAll(pageable: Pageable): Page<TypedCSVRecord> =
        data.values.sortedWith(defaultComparator).paged(pageable)

    override fun findAllByCSVID(csvId: CSVID, pageable: Pageable): Page<TypedCSVRecord> =
        data.values.filter { it.csvId == csvId }
            .sortedWith(defaultComparator)
            .paged(pageable)

    override fun existsById(id: UUID): Boolean =
        id in data

    override fun deleteById(id: UUID) {
        data.remove(id)
    }

    override fun delete(entity: TypedCSVRecord) =
        deleteById(entity.id)

    override fun deleteAll() =
        data.clear()

    override fun deleteAll(entities: Iterable<TypedCSVRecord>) =
        entities.forEach(::delete)

    override fun deleteAllById(ids: MutableIterable<UUID>) =
        ids.forEach(::deleteById)

    override fun deleteAllByCSVID(csvId: CSVID) {
        data.values.filter { it.csvId == csvId }.forEach { data.remove(it.id) }
    }

    override fun count(): Long =
        data.size.toLong()
}
