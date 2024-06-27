package org.orkg.graph.output

import java.net.URI
import org.orkg.graph.domain.ExternalThing

interface ExternalResourceService {
    fun findResourceByShortForm(ontologyId: String, shortForm: String): ExternalThing?
    fun findResourceByURI(ontologyId: String, uri: URI): ExternalThing?
    fun supportsOntology(ontologyId: String): Boolean
    fun supportsMultipleOntologies(): Boolean
}
