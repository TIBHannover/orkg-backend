package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.ClassRepresentation
import eu.tib.orkg.prototype.statements.domain.model.Class
import java.util.*
import org.springframework.data.domain.Page

interface ClassRepresentationAdapter {
    fun Optional<Class>.mapToClassRepresentation(): Optional<ClassRepresentation> =
        map { it.toClassRepresentation() }

    fun Page<Class>.mapToClassRepresentation(): Page<ClassRepresentation> =
        map { it.toClassRepresentation() }

    fun Class.toClassRepresentation() =
        ClassRepresentation(id, label, uri, description, createdAt, createdBy)
}
