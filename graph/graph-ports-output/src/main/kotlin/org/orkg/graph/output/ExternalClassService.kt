package org.orkg.graph.output

import java.net.URI
import org.orkg.graph.domain.ExternalThing

interface ExternalClassService {
    fun findClassByShortForm(ontologyId: String, shortForm: String): ExternalThing?
    fun findClassByURI(ontologyId: String, uri: URI): ExternalThing?
    fun supportsOntology(ontologyId: String): Boolean
    fun supportsMultipleOntologies(): Boolean
}
