package org.orkg.graph.domain

import org.eclipse.rdf4j.common.net.ParsedIRI

data class ExternalThing(
    val uri: ParsedIRI,
    val label: String,
    val description: String?
)
