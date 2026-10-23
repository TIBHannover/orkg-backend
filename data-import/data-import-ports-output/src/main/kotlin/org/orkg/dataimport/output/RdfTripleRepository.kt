package org.orkg.dataimport.output

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Literal
import org.eclipse.rdf4j.model.Resource
import org.orkg.dataimport.domain.rdf.RdfTriple
import org.orkg.dataimport.domain.rdf.ResourceToTypeMapping
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.UUID

interface RdfTripleRepository :
    CrudRepository<RdfTriple, UUID>,
    PagingAndSortingRepository<RdfTriple, UUID> {
    fun findAllByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<RdfTriple>

    fun findAllDistinctIndividualsByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<Resource>

    fun findAllDistinctPredicatesByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<IRI>

    fun findAllDistinctClassIrisByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<IRI>

    fun findAllDistinctObjectIrisByRdfFileIdAndSubjectAndPredicate(rdfFileId: UUID, subject: Resource, predicate: IRI): Set<IRI>

    fun findAllDistinctObjectLiteralsByRdfFileIdAndSubjectAndPredicate(rdfFileId: UUID, subject: Resource, predicate: IRI): Set<Literal>

    fun findAllDistinctResourceToTypeMappingsByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<ResourceToTypeMapping>

    fun deleteAllByRdfFileId(rdfFileId: UUID)
}
