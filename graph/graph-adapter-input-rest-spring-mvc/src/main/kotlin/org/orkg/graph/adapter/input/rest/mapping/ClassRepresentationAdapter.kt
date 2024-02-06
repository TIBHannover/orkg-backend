package org.orkg.graph.adapter.input.rest.mapping

import java.util.*
import org.orkg.graph.domain.Class
import org.orkg.graph.input.ClassRepresentation
import org.springframework.data.domain.Page

interface ClassRepresentationAdapter {
    fun Optional<Class>.mapToClassRepresentation(): Optional<ClassRepresentation> =
        map { it.toClassRepresentation() }

    fun Page<Class>.mapToClassRepresentation(): Page<ClassRepresentation> =
        map { it.toClassRepresentation() }

    fun Class.toClassRepresentation() =
        ClassRepresentation(id, label, uri, description, createdAt, createdBy, modifiable)
}
