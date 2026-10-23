package org.orkg.dataimport.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.orkg.dataimport.adapter.output.jpa.parseResource
import org.orkg.dataimport.domain.rdf.RdfResource
import java.util.UUID

@Entity
@Table(name = "rdf_resources")
class RdfResourceEntity {
    @Id
    @Column(nullable = false)
    var id: UUID? = null

    @Column(nullable = false)
    var iri: String? = null

    @Column(name = "rdf_file_id", nullable = false)
    var rdfFileId: UUID? = null

    var label: String? = null

    @Column(nullable = false)
    var classes: Set<String>? = null

    fun toRdfResource() =
        RdfResource(
            id = id!!,
            iri = parseResource(iri!!),
            rdfFileId = rdfFileId!!,
            label = label,
            classes = classes!!.map(SimpleValueFactory.getInstance()::createIRI).toSet(),
        )
}
