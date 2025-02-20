package org.orkg.graph.output

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.graph.domain.ExternalThing

interface ExternalResourceService {
    fun findResourceByShortForm(ontologyId: String, shortForm: String): ExternalThing?

    fun findResourceByURI(ontologyId: String, uri: ParsedIRI): ExternalThing?

    fun supportsOntology(ontologyId: String): Boolean

    fun supportsMultipleOntologies(): Boolean
}
