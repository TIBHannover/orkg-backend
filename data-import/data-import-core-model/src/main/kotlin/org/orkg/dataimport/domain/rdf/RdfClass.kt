package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.model.IRI
import java.util.UUID

data class RdfClass(
    val id: UUID,
    val rdfFileId: UUID,
    val iri: IRI,
    val label: String?,
)
