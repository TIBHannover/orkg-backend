package org.orkg.dataimport.domain.rdf

import org.orkg.common.IRI

data class ResourceToTypeMapping(
    val iri: IRI,
    val type: OrkgEntityType,
) {
    enum class OrkgEntityType {
        CLASS,
        PREDICATE,
        RESOURCE,
    }
}
