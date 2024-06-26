package org.orkg.graph.output

import java.net.URI
import org.orkg.graph.domain.ExternalThing

interface ExternalPredicateService {
    fun findPredicateByShortForm(ontologyId: String, shortForm: String): ExternalThing?
    fun findPredicateByURI(ontologyId: String, uri: URI): ExternalThing?
    fun supportsOntology(ontologyId: String): Boolean
    fun supportsMultipleOntologies(): Boolean
}
