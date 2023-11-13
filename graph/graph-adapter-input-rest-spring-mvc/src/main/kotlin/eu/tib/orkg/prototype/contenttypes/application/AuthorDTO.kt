package eu.tib.orkg.prototype.contenttypes.application

import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.net.URI
import javax.validation.constraints.NotBlank

data class AuthorDTO(
    val id: ThingId?,
    @NotBlank
    val name: String,
    val identifiers: Map<String, String>?,
    val homepage: URI?
) {
    fun toCreateCommand(): Author =
        Author(
            id = id,
            name = name,
            identifiers = identifiers,
            homepage = homepage
        )
}
