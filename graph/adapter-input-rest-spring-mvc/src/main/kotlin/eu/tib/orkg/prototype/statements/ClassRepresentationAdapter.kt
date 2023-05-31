package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.net.URI
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page

interface ClassRepresentationAdapter {
    fun Optional<Class>.mapToClassRepresentation(): Optional<ClassRepresentation> =
        map { it.toClassRepresentation() }

    fun Page<Class>.mapToClassRepresentation(): Page<ClassRepresentation> =
        map { it.toClassRepresentation() }

    fun Class.toClassRepresentation() =
        object : ClassRepresentation {
            override val id: ThingId = this@toClassRepresentation.id
            override val label: String = this@toClassRepresentation.label
            override val uri: URI? = this@toClassRepresentation.uri
            override val description: String? = this@toClassRepresentation.description
            override val jsonClass: String = "class"
            override val createdAt: OffsetDateTime = this@toClassRepresentation.createdAt
            override val createdBy: ContributorId = this@toClassRepresentation.createdBy
        }
}
