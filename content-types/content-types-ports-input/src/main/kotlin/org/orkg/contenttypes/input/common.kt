package org.orkg.contenttypes.input

import java.net.URI

data class PublicationInfoDefinition(
    val publishedMonth: Int?,
    val publishedYear: Long?,
    val publishedIn: String?,
    val url: URI?
)
