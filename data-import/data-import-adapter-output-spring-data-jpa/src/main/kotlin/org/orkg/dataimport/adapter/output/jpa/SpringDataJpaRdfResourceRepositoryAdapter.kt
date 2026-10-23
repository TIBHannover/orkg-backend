package org.orkg.dataimport.adapter.output.jpa

import org.orkg.common.withDefaultSort
import org.orkg.dataimport.adapter.output.jpa.internal.PostgresRdfResourceRepository
import org.orkg.dataimport.adapter.output.jpa.internal.RdfResourceEntity
import org.orkg.dataimport.domain.rdf.RdfResource
import org.orkg.dataimport.output.RdfResourceRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
@TransactionalOnJPA
class SpringDataJpaRdfResourceRepositoryAdapter(
    private val postgresRepository: PostgresRdfResourceRepository,
) : RdfResourceRepository {
    override fun <S : RdfResource> save(entity: S): S {
        postgresRepository.save(entity.toRdfResourceEntity())
        return entity
    }

    override fun <S : RdfResource> saveAll(entities: Iterable<S>): Iterable<S> {
        postgresRepository.saveAll(entities.map { it.toRdfResourceEntity() })
        return entities
    }

    override fun findById(id: UUID): Optional<RdfResource> =
        postgresRepository.findById(id).map { it.toRdfResource() }

    override fun findAll(): Iterable<RdfResource> =
        postgresRepository.findAll().map { it.toRdfResource() }

    override fun findAll(sort: Sort): Iterable<RdfResource> =
        postgresRepository.findAll(sort).map { it.toRdfResource() }

    override fun findAll(pageable: Pageable): Page<RdfResource> =
        postgresRepository.findAll(pageable.withDefaultSort { Sort.by("iri").ascending() }).map { it.toRdfResource() }

    override fun findAllById(ids: Iterable<UUID>): Iterable<RdfResource> =
        postgresRepository.findAllById(ids).map { it.toRdfResource() }

    override fun findAllByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<RdfResource> =
        postgresRepository.findAllByRdfFileId(rdfFileId, pageable.withDefaultSort { Sort.by("iri").ascending() }).map { it.toRdfResource() }

    override fun existsById(id: UUID): Boolean =
        postgresRepository.existsById(id)

    override fun delete(entity: RdfResource) =
        postgresRepository.deleteById(entity.id)

    override fun deleteById(id: UUID) =
        postgresRepository.deleteById(id)

    override fun deleteAll(entities: Iterable<RdfResource>) =
        postgresRepository.deleteAllById(entities.map { it.id })

    override fun deleteAllById(ids: Iterable<UUID>) =
        postgresRepository.deleteAllById(ids)

    override fun deleteAll() =
        postgresRepository.deleteAll()

    override fun deleteAllByRdfFileId(rdfFileId: UUID) =
        postgresRepository.deleteAllByRdfFileId(rdfFileId)

    override fun count(): Long =
        postgresRepository.count()

    private fun RdfResource.toRdfResourceEntity(): RdfResourceEntity =
        postgresRepository.findById(id).orElseGet(::RdfResourceEntity).apply {
            id = this@toRdfResourceEntity.id
            iri = this@toRdfResourceEntity.iri.toString()
            rdfFileId = this@toRdfResourceEntity.rdfFileId
            label = this@toRdfResourceEntity.label
            classes = this@toRdfResourceEntity.classes.map { it.toString() }.toSet()
        }
}
