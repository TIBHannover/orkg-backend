package org.orkg.dataimport.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.rio.helpers.NTriplesUtil
import org.orkg.dataimport.domain.rdf.RdfTriple
import java.util.UUID

@Entity
@Table(name = "rdf_triples")
class RdfTripleEntity {
    @Id
    @Column(nullable = false)
    var id: UUID? = null

    @Column(name = "rdf_file_id", nullable = false)
    var rdfFileId: UUID? = null

    @Column(nullable = false)
    var subject: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false)
    var subjectType: Value.Type? = null

    @Column(nullable = false)
    var predicate: String? = null

    @Column(nullable = false)
    var `object`: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "object_type", nullable = false)
    var objectType: Value.Type? = null

    fun toRdfTriple() =
        RdfTriple(
            id = id!!,
            rdfFileId = rdfFileId!!,
            subject = parseResource(subject!!, subjectType!!),
            predicate = SimpleValueFactory.getInstance().createIRI(predicate!!),
            `object` = parseValue(`object`!!, objectType!!),
        )
}
