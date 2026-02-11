package org.orkg.contenttypes.output

import org.orkg.common.DOI
import org.orkg.contenttypes.domain.Author
import tools.jackson.databind.JsonNode
import java.net.URI
import java.util.Optional

interface DoiService {
    fun findMetadataByDoi(doi: DOI): Optional<JsonNode>

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
