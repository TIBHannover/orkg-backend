package org.orkg.contenttypes.output

import org.orkg.common.DOI
import org.orkg.contenttypes.domain.Author
import java.net.URI

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
        val relatedIdentifiers: List<String>,
    )
}
