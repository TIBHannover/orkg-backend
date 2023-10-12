package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.LiteralRepresentation
import eu.tib.orkg.prototype.statements.domain.model.Literal
import java.util.*
import org.springframework.data.domain.Page

interface LiteralRepresentationAdapter {

    fun Optional<Literal>.mapToLiteralRepresentation(): Optional<LiteralRepresentation> =
        map { it.toLiteralRepresentation() }

    fun Page<Literal>.mapToLiteralRepresentation(): Page<LiteralRepresentation> =
        map { it.toLiteralRepresentation() }

    fun Literal.toLiteralRepresentation(): LiteralRepresentation =
        LiteralRepresentation(id, label, datatype, createdAt, createdBy)
}
