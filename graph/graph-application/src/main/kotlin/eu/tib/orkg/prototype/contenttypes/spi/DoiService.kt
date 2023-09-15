package eu.tib.orkg.prototype.contenttypes.spi

import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.domain.model.DOI
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
        val relatedIdentifiers: List<String>
    )
}
