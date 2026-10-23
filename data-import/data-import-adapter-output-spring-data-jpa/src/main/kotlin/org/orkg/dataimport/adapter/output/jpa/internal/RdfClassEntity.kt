package org.orkg.dataimport.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.orkg.dataimport.domain.rdf.RdfClass
import java.util.UUID

@Entity
@Table(name = "rdf_classes")
class RdfClassEntity {
    @Id
    @Column(nullable = false)
    var id: UUID? = null

    @Column(nullable = false)
    var iri: String? = null

    @Column(name = "rdf_file_id", nullable = false)
    var rdfFileId: UUID? = null

    var label: String? = null

    fun toRdfClass() =
        RdfClass(
            id = id!!,
            iri = SimpleValueFactory.getInstance().createIRI(iri!!),
            rdfFileId = rdfFileId!!,
            label = label,
        )
}
