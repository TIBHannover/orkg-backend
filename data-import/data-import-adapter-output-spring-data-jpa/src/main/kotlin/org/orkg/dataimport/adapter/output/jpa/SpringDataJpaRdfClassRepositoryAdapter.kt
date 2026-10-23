package org.orkg.dataimport.adapter.output.jpa

import org.orkg.common.withDefaultSort
import org.orkg.dataimport.adapter.output.jpa.internal.PostgresRdfClassRepository
import org.orkg.dataimport.adapter.output.jpa.internal.RdfClassEntity
import org.orkg.dataimport.domain.rdf.RdfClass
import org.orkg.dataimport.output.RdfClassRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
@TransactionalOnJPA
class SpringDataJpaRdfClassRepositoryAdapter(
    private val postgresRepository: PostgresRdfClassRepository,
) : RdfClassRepository {
    override fun <S : RdfClass> save(entity: S): S {
        postgresRepository.save(entity.toRdfClassEntity())
        return entity
    }

    override fun <S : RdfClass> saveAll(entities: Iterable<S>): Iterable<S> {
        postgresRepository.saveAll(entities.map { it.toRdfClassEntity() })
        return entities
    }

    override fun findById(id: UUID): Optional<RdfClass> =
        postgresRepository.findById(id).map { it.toRdfClass() }

    override fun findAll(): Iterable<RdfClass> =
        postgresRepository.findAll().map { it.toRdfClass() }

    override fun findAll(sort: Sort): Iterable<RdfClass> =
        postgresRepository.findAll(sort).map { it.toRdfClass() }

    override fun findAll(pageable: Pageable): Page<RdfClass> =
        postgresRepository.findAll(pageable.withDefaultSort { Sort.by("iri").ascending() }).map { it.toRdfClass() }

    override fun findAllById(ids: Iterable<UUID>): Iterable<RdfClass> =
        postgresRepository.findAllById(ids).map { it.toRdfClass() }

    override fun findAllByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<RdfClass> =
        postgresRepository.findAllByRdfFileId(rdfFileId, pageable.withDefaultSort { Sort.by("iri").ascending() }).map { it.toRdfClass() }

    override fun existsById(id: UUID): Boolean =
        postgresRepository.existsById(id)

    override fun delete(entity: RdfClass) =
        postgresRepository.deleteById(entity.id)

    override fun deleteById(id: UUID) =
        postgresRepository.deleteById(id)

    override fun deleteAll(entities: Iterable<RdfClass>) =
        postgresRepository.deleteAllById(entities.map { it.id })

    override fun deleteAllById(ids: Iterable<UUID>) =
        postgresRepository.deleteAllById(ids)

    override fun deleteAll() =
        postgresRepository.deleteAll()

    override fun deleteAllByRdfFileId(rdfFileId: UUID) =
        postgresRepository.deleteAllByRdfFileId(rdfFileId)

    override fun count(): Long =
        postgresRepository.count()

    private fun RdfClass.toRdfClassEntity(): RdfClassEntity =
        postgresRepository.findById(id).orElseGet(::RdfClassEntity).apply {
            id = this@toRdfClassEntity.id
            iri = this@toRdfClassEntity.iri.toString()
            rdfFileId = this@toRdfClassEntity.rdfFileId
            label = this@toRdfClassEntity.label
        }
}
