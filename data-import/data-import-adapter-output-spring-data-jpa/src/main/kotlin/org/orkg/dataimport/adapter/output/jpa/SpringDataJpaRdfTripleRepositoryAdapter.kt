package org.orkg.dataimport.adapter.output.jpa

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Literal
import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.rio.helpers.NTriplesUtil
import org.orkg.common.withDefaultSort
import org.orkg.dataimport.adapter.output.jpa.internal.PostgresRdfTripleRepository
import org.orkg.dataimport.adapter.output.jpa.internal.RdfTripleEntity
import org.orkg.dataimport.domain.rdf.RdfTriple
import org.orkg.dataimport.domain.rdf.ResourceToTypeMapping
import org.orkg.dataimport.domain.rdf.ResourceToTypeMapping.OrkgEntityType
import org.orkg.dataimport.output.RdfTripleRepository
import org.orkg.spring.data.annotations.TransactionalOnJPA
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID
import kotlin.collections.map

@Component
@TransactionalOnJPA
class SpringDataJpaRdfTripleRepositoryAdapter(
    private val postgresRepository: PostgresRdfTripleRepository,
) : RdfTripleRepository {
    override fun <S : RdfTriple> save(entity: S): S {
        postgresRepository.save(entity.toRdfTripleEntity())
        return entity
    }

    override fun <S : RdfTriple> saveAll(entities: Iterable<S>): Iterable<S> {
        postgresRepository.saveAll(entities.map { it.toRdfTripleEntity() })
        return entities
    }

    override fun findById(id: UUID): Optional<RdfTriple> =
        postgresRepository.findById(id).map { it.toRdfTriple() }

    override fun findAll(): Iterable<RdfTriple> =
        postgresRepository.findAll().map { it.toRdfTriple() }

    override fun findAll(sort: Sort): Iterable<RdfTriple> =
        postgresRepository.findAll(sort).map { it.toRdfTriple() }

    override fun findAll(pageable: Pageable): Page<RdfTriple> =
        postgresRepository.findAll(pageable.withDefaultSort { Sort.by("id").ascending() }).map { it.toRdfTriple() }

    override fun findAllById(ids: Iterable<UUID>): Iterable<RdfTriple> =
        postgresRepository.findAllById(ids).map { it.toRdfTriple() }

    override fun findAllByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<RdfTriple> =
        postgresRepository.findAllByRdfFileId(rdfFileId, pageable.withDefaultSort { Sort.by("id").ascending() })
            .map { it.toRdfTriple() }

    override fun findAllDistinctIndividualsByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<Resource> =
        postgresRepository.findAllDistinctIndividualsByRdfFileId(rdfFileId, pageable.withDefaultSort { Sort.by("individual").ascending() })
            .map(::parseResource)

    override fun findAllDistinctPredicatesByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<IRI> =
        postgresRepository.findAllDistinctPredicatesByRdfFileId(rdfFileId, pageable.withDefaultSort { Sort.by("predicate").ascending() })
            .map(SimpleValueFactory.getInstance()::createIRI)

    override fun findAllDistinctClassIrisByRdfFileId(
        rdfFileId: UUID,
        pageable: Pageable,
    ): Page<IRI> =
        postgresRepository.findAllDistinctClassIrisByRdfFileId(
            rdfFileId = rdfFileId,
            pageable = pageable.withDefaultSort { Sort.by("class").ascending() },
        )
            .map(SimpleValueFactory.getInstance()::createIRI)

    override fun findAllDistinctObjectIrisByRdfFileIdAndSubjectAndPredicate(
        rdfFileId: UUID,
        subject: Resource,
        predicate: IRI,
    ): Set<IRI> =
        postgresRepository.findAllDistinctObjectIrisByRdfFileIdAndSubjectAndPredicate(
            rdfFileId = rdfFileId,
            subject = subject.toString(),
            predicate = predicate.toString(),
        )
            .map(SimpleValueFactory.getInstance()::createIRI)
            .toSet()

    override fun findAllDistinctObjectLiteralsByRdfFileIdAndSubjectAndPredicate(
        rdfFileId: UUID,
        subject: Resource,
        predicate: IRI,
    ): Set<Literal> =
        postgresRepository.findAllDistinctObjectsByRdfFileIdAndSubjectAndPredicate(
            rdfFileId = rdfFileId,
            subject = subject.toString(),
            predicate = predicate.toString(),
            objectType = Value.Type.Literal,
        )
            .map { NTriplesUtil.parseLiteral(it, SimpleValueFactory.getInstance()) }
            .toSet()

    override fun findAllDistinctResourceToTypeMappingsByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<ResourceToTypeMapping> =
        postgresRepository.findAllDistinctResourceToTypeMappingsByRdfFileId(
            rdfFileId = rdfFileId,
            pageable = pageable.withDefaultSort { Sort.by("iri").ascending() },
        )
            .map {
                ResourceToTypeMapping(
                    iri = org.orkg.common.IRI.create(it.iri!!),
                    type = OrkgEntityType.valueOf(it.type!!.uppercase()),
                )
            }

    override fun existsById(id: UUID): Boolean =
        postgresRepository.existsById(id)

    override fun delete(entity: RdfTriple) =
        postgresRepository.deleteById(entity.id)

    override fun deleteById(id: UUID) =
        postgresRepository.deleteById(id)

    override fun deleteAll() =
        postgresRepository.deleteAll()

    override fun deleteAll(entities: Iterable<RdfTriple>) =
        postgresRepository.deleteAllById(entities.map { it.id })

    override fun deleteAllById(ids: Iterable<UUID>) =
        postgresRepository.deleteAllById(ids)

    override fun deleteAllByRdfFileId(rdfFileId: UUID) =
        postgresRepository.deleteAllByRdfFileId(rdfFileId)

    override fun count(): Long =
        postgresRepository.count()

    private fun RdfTriple.toRdfTripleEntity(): RdfTripleEntity =
        postgresRepository.findById(id).orElseGet(::RdfTripleEntity).apply {
            id = this@toRdfTripleEntity.id
            rdfFileId = this@toRdfTripleEntity.rdfFileId
            subject = this@toRdfTripleEntity.subject.toString()
            subjectType = this@toRdfTripleEntity.subject.type
            predicate = this@toRdfTripleEntity.predicate.toString()
            `object` = this@toRdfTripleEntity.`object`.toString()
            objectType = this@toRdfTripleEntity.`object`.type
        }
}
