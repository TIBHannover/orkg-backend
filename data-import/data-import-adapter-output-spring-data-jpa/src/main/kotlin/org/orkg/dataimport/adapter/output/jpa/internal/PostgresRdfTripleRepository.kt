package org.orkg.dataimport.adapter.output.jpa.internal

import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.vocabulary.RDF
import org.eclipse.rdf4j.model.vocabulary.RDFS
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface PostgresRdfTripleRepository : JpaRepository<RdfTripleEntity, UUID> {
    fun findAllByRdfFileId(rdfFileId: UUID, pageable: Pageable): Page<RdfTripleEntity>

    @Query(
        """
SELECT DISTINCT subject AS individual FROM RdfTripleEntity
WHERE rdfFileId = :rdfFileId AND (subjectType = 'IRI' OR subjectType = 'BNode') AND predicate <> '${RDFS.NAMESPACE}subClassOf'
UNION
SELECT DISTINCT (object) AS individual FROM RdfTripleEntity
WHERE rdfFileId = :rdfFileId AND (objectType = 'IRI' OR objectType = 'BNode') AND predicate <> '${RDFS.NAMESPACE}subClassOf' AND predicate <> '${RDF.NAMESPACE}type'""",
    )
    fun findAllDistinctIndividualsByRdfFileId(
        rdfFileId: UUID,
        pageable: Pageable,
    ): Page<String>

    @Query(
        """
SELECT DISTINCT predicate FROM RdfTripleEntity
WHERE rdfFileId = :rdfFileId""",
    )
    fun findAllDistinctPredicatesByRdfFileId(
        rdfFileId: UUID,
        pageable: Pageable,
    ): Page<String>

    @Query(
        """
SELECT DISTINCT (object) AS class FROM RdfTripleEntity
WHERE rdfFileId = :rdfFileId AND objectType = 'IRI' AND (predicate = '${RDF.NAMESPACE}type' OR predicate = '${RDFS.NAMESPACE}subClassOf')
UNION
SELECT DISTINCT subject AS class FROM RdfTripleEntity
WHERE rdfFileId = :rdfFileId AND subjectType = 'IRI' AND predicate = '${RDFS.NAMESPACE}subClassOf'""",
    )
    fun findAllDistinctClassIrisByRdfFileId(
        rdfFileId: UUID,
        pageable: Pageable,
    ): Page<String>

    @Query(
        """
SELECT DISTINCT (object) FROM RdfTripleEntity
WHERE rdfFileId = :rdfFileId AND subject = :subject AND predicate = :predicate AND objectType = 'IRI'
ORDER BY object ASC""",
    )
    fun findAllDistinctObjectIrisByRdfFileIdAndSubjectAndPredicate(
        rdfFileId: UUID,
        subject: String,
        predicate: String,
    ): Set<String>

    @Query(
        """
SELECT DISTINCT (object) FROM RdfTripleEntity
WHERE rdfFileId = :rdfFileId AND subject = :subject AND predicate = :predicate AND objectType = :objectType
ORDER BY object ASC""",
    )
    fun findAllDistinctObjectsByRdfFileIdAndSubjectAndPredicate(
        rdfFileId: UUID,
        subject: String,
        predicate: String,
        objectType: Value.Type,
    ): Set<String>

    @Query(
        value = """
SELECT DISTINCT iri, 'predicate' AS type FROM rdf_predicates
WHERE rdf_file_id = :rdfFileId
UNION ALL
SELECT DISTINCT iri, 'class' AS type FROM rdf_classes
WHERE rdf_file_id = :rdfFileId
UNION ALL
SELECT DISTINCT iri, 'resource' AS type FROM rdf_resources
WHERE rdf_file_id = :rdfFileId""",
        nativeQuery = true,
    )
    fun findAllDistinctResourceToTypeMappingsByRdfFileId(
        rdfFileId: UUID,
        pageable: Pageable,
    ): Page<ResourceToTypeMappingEntity>

    fun deleteAllByRdfFileId(rdfFileId: UUID)
}
