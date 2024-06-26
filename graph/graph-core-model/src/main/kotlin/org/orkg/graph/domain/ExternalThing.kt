package org.orkg.graph.domain

import java.net.URI

data class ExternalThing(
    val uri: URI,
    val label: String,
    val description: String?
)
