package org.orkg.graph.domain

import org.orkg.common.IRI

data class ExternalThing(
    val uri: IRI,
    val label: String,
    val description: String?,
)
