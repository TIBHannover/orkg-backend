package org.orkg.graph.output

import org.orkg.common.IRI
import org.orkg.graph.domain.ExternalThing

interface ExternalResourceService {
    fun findResourceByShortForm(ontologyId: String, shortForm: String): ExternalThing?

    fun findResourceByURI(ontologyId: String, uri: IRI): ExternalThing?

    fun supportsOntology(ontologyId: String): Boolean

    fun supportsMultipleOntologies(): Boolean
}
