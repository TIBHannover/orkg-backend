package org.orkg.dataimport.domain.rdf

import org.eclipse.rdf4j.model.IRI
import org.eclipse.rdf4j.model.Resource
import org.eclipse.rdf4j.model.Value
import java.util.UUID

data class RdfTriple(
    val id: UUID,
    val rdfFileId: UUID,
    val subject: Resource,
    val predicate: IRI,
    val `object`: Value,
)
