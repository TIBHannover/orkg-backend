package org.orkg.dataimport.adapter.output.inmemory

import org.orkg.common.paged
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecord
import org.orkg.dataimport.output.PaperCSVRecordRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class InMemoryPaperCSVRecordRepositoryAdapter : PaperCSVRecordRepository {
    private val data = mutableMapOf<UUID, PaperCSVRecord>()
    private val defaultComparator = Comparator.comparing<PaperCSVRecord, UUID> { it.csvId.value }
        .thenComparing(Comparator.comparing<PaperCSVRecord, Long> { it.itemNumber })

    override fun <S : PaperCSVRecord> save(entity: S): S {
        data[entity.id] = entity
        return entity
    }

    override fun <S : PaperCSVRecord> saveAll(entities: Iterable<S>): Iterable<S> =
        entities.map(::save)

    override fun findById(id: UUID): Optional<PaperCSVRecord> =
        Optional.ofNullable(data[id])

    override fun findAll(): Iterable<PaperCSVRecord> =
        data.values

    override fun findAll(sort: Sort): Iterable<PaperCSVRecord> =
        findAll().sortedWith(defaultComparator)

    override fun findAll(pageable: Pageable): Page<PaperCSVRecord> =
        data.values.sortedWith(defaultComparator).paged(pageable)

    override fun findAllById(ids: Iterable<UUID>): Iterable<PaperCSVRecord> =
        data.values.filter { it.id in ids }

    override fun findAllByCSVID(csvId: CSVID, pageable: Pageable): Page<PaperCSVRecord> =
        data.values.filter { it.csvId == csvId }
            .sortedWith(defaultComparator)
            .paged(pageable)

    override fun existsById(id: UUID): Boolean =
        id in data

    override fun delete(entity: PaperCSVRecord) =
        deleteById(entity.id)

    override fun deleteById(id: UUID) {
        data.remove(id)
    }

    override fun deleteAll() =
        data.clear()

    override fun deleteAll(entities: Iterable<PaperCSVRecord>) =
        entities.forEach(::delete)

    override fun deleteAllById(ids: MutableIterable<UUID>) =
        ids.forEach(::deleteById)

    override fun deleteAllByCSVID(csvId: CSVID) {
        data.values.filter { it.csvId == csvId }.forEach { data.remove(it.id) }
    }

    override fun count(): Long =
        data.size.toLong()
}
