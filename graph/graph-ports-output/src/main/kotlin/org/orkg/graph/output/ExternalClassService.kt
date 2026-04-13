package org.orkg.graph.output

import org.orkg.common.IRI
import org.orkg.graph.domain.ExternalThing

interface ExternalClassService {
    fun findClassByShortForm(ontologyId: String, shortForm: String): ExternalThing?

    fun findClassByURI(ontologyId: String, uri: IRI): ExternalThing?

    fun supportsOntology(ontologyId: String): Boolean

    fun supportsMultipleOntologies(): Boolean
}
