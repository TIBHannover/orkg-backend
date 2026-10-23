package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Resource
import java.util.UUID

data class RdfResource(
    val id: UUID,
    val rdfFileId: UUID,
    val iri: Resource,
    val label: String?,
    val classes: Set<IRI>,
)
