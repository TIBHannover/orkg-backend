package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page

interface LiteralRepresentationAdapter {

    fun Optional<Literal>.mapToLiteralRepresentation(): Optional<LiteralRepresentation> =
        map { it.toLiteralRepresentation() }

    fun Page<Literal>.mapToLiteralRepresentation(): Page<LiteralRepresentation> =
        map { it.toLiteralRepresentation() }

    fun Literal.toLiteralRepresentation(): LiteralRepresentation =
        object : LiteralRepresentation {
            override val id: ThingId = this@toLiteralRepresentation.id
            override val label: String = this@toLiteralRepresentation.label
            override val datatype: String = this@toLiteralRepresentation.datatype
            override val jsonClass: String = "literal"
            override val createdAt: OffsetDateTime = this@toLiteralRepresentation.createdAt
            override val createdBy: ContributorId = this@toLiteralRepresentation.createdBy
        }
}
