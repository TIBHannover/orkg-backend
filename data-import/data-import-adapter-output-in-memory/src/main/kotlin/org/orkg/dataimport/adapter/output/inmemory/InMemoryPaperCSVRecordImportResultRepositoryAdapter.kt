package org.orkg.dataimport.adapter.output.inmemory

import org.orkg.common.paged
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordImportResult
import org.orkg.dataimport.output.PaperCSVRecordImportResultRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class InMemoryPaperCSVRecordImportResultRepositoryAdapter : PaperCSVRecordImportResultRepository {
    private val data = mutableMapOf<UUID, PaperCSVRecordImportResult>()
    private val defaultComparator = Comparator.comparing<PaperCSVRecordImportResult, UUID> { it.csvId.value }
        .thenComparing(Comparator.comparing<PaperCSVRecordImportResult, Long> { it.itemNumber })

    override fun <S : PaperCSVRecordImportResult> save(entity: S): S {
        data[entity.id] = entity
        return entity
    }

    override fun <S : PaperCSVRecordImportResult> saveAll(entities: Iterable<S>): Iterable<S> =
        entities.map(::save)

    override fun findById(id: UUID): Optional<PaperCSVRecordImportResult> =
        Optional.ofNullable(data[id])

    override fun findAll(): Iterable<PaperCSVRecordImportResult> =
        data.values

    override fun findAllById(ids: Iterable<UUID>): Iterable<PaperCSVRecordImportResult> =
        data.values.filter { it.id in ids }

    override fun findAllByCSVID(csvId: CSVID, pageable: Pageable): Page<PaperCSVRecordImportResult> =
        data.values.filter { it.csvId == csvId }
            .sortedWith(defaultComparator)
            .paged(pageable)

    override fun deleteAllByCSVID(csvId: CSVID) {
        data.values.filter { it.csvId == csvId }.forEach { data.remove(it.id) }
    }

    override fun existsById(id: UUID): Boolean =
        id in data

    override fun delete(entity: PaperCSVRecordImportResult) =
        deleteById(entity.id)

    override fun deleteById(id: UUID) {
        data.remove(id)
    }

    override fun deleteAll() =
        data.clear()

    override fun deleteAll(entities: Iterable<PaperCSVRecordImportResult>) =
        entities.forEach(::delete)

    override fun deleteAllById(ids: MutableIterable<UUID>) =
        ids.forEach(::deleteById)

    override fun count(): Long =
        data.size.toLong()
}
