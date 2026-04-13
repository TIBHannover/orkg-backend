package org.orkg.graph.output

import org.orkg.common.IRI
import org.orkg.graph.domain.ExternalThing

interface ExternalPredicateService {
    fun findPredicateByShortForm(ontologyId: String, shortForm: String): ExternalThing?

    fun findPredicateByURI(ontologyId: String, uri: IRI): ExternalThing?

    fun supportsOntology(ontologyId: String): Boolean

    fun supportsMultipleOntologies(): Boolean
}
