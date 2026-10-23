package org.orkg.dataimport.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.orkg.dataimport.domain.rdf.RdfPredicate
import java.util.UUID

@Entity
@Table(name = "rdf_predicates")
class RdfPredicateEntity {
    @Id
    @Column(nullable = false)
    var id: UUID? = null

    @Column(nullable = false)
    var iri: String? = null

    @Column(name = "rdf_file_id", nullable = false)
    var rdfFileId: UUID? = null

    var label: String? = null

    fun toRdfPredicate() =
        RdfPredicate(
            id = id!!,
            iri = SimpleValueFactory.getInstance().createIRI(iri!!),
            rdfFileId = rdfFileId!!,
            label = label,
        )
}
