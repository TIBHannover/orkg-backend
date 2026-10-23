package org.orkg.dataimport.adapter.output.jpa

import org.orkg.common.withDefaultSort
import org.orkg.dataimport.adapter.output.jpa.internal.PostgresRdfPredicateRepository
import org.orkg.dataimport.adapter.output.jpa.internal.RdfPredicateEntity
import org.orkg.dataimport.domain.rdf.RdfPredicate
import org.orkg.dataimport.output.RdfPredicateRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
@TransactionalOnJPA
class SpringDataJpaRdfPredicateRepositoryAdapter(
    private val postgresRepository: PostgresRdfPredicateRepository,
) : RdfPredicateRepository {
    override fun <S : RdfPredicate> save(entity: S): S {
        postgresRepository.save(entity.toRdfPredicateEntity())
        return entity
    }

    override fun <S : RdfPredicate> saveAll(entities: Iterable<S>): Iterable<S> {
        postgresRepository.saveAll(entities.map { it.toRdfPredicateEntity() })
        return entities
    }

    override fun findById(id: UUID): Optional<RdfPredicate> =
        postgresRepository.findById(id).map { it.toRdfPredicate() }

    override fun findAll(): Iterable<RdfPredicate> =
        postgresRepository.findAll().map { it.toRdfPredicate() }

    override fun findAll(sort: Sort): Iterable<RdfPredicate> =
        postgresRepository.findAll(sort).map { it.toRdfPredicate() }

    override fun findAll(pageable: Pageable): Page<RdfPredicate> =
        postgresRepository.findAll(pageable.withDefaultSort { Sort.by("iri").ascending() }).map { it.toRdfPredicate() }

    override fun findAllById(ids: Iterable<UUID>): Iterable<RdfPredicate> =
        postgresRepository.findAllById(ids).map { it.toRdfPredicate() }

    override fun findAllByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<RdfPredicate> =
        postgresRepository.findAllByRdfFileId(rdfFileId, pageable.withDefaultSort { Sort.by("iri").ascending() }).map { it.toRdfPredicate() }

    override fun existsById(id: UUID): Boolean =
        postgresRepository.existsById(id)

    override fun delete(entity: RdfPredicate) =
        postgresRepository.deleteById(entity.id)

    override fun deleteById(id: UUID) =
        postgresRepository.deleteById(id)

    override fun deleteAll(entities: Iterable<RdfPredicate>) =
        postgresRepository.deleteAllById(entities.map { it.id })

    override fun deleteAllById(ids: Iterable<UUID>) =
        postgresRepository.deleteAllById(ids)

    override fun deleteAll() =
        postgresRepository.deleteAll()

    override fun deleteAllByRdfFileId(rdfFileId: UUID) =
        postgresRepository.deleteAllByRdfFileId(rdfFileId)

    override fun count(): Long =
        postgresRepository.count()

    private fun RdfPredicate.toRdfPredicateEntity(): RdfPredicateEntity =
        postgresRepository.findById(id).orElseGet(::RdfPredicateEntity).apply {
            id = this@toRdfPredicateEntity.id
            iri = this@toRdfPredicateEntity.iri.toString()
            rdfFileId = this@toRdfPredicateEntity.rdfFileId
            label = this@toRdfPredicateEntity.label
        }
}
