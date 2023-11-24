package org.orkg.contenttypes.output

import java.net.URI
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.identifiers.DOI

interface DoiService {
    fun register(command: RegisterCommand): DOI

    data class RegisterCommand(
        val suffix: String,
        val title: String,
        val subject: String,
        val description: String,
        val url: URI,
        val creators: List<Author>,
        val resourceType: String,
        val resourceTypeGeneral: String,
        val relatedIdentifiers: List<String>
    )
}
