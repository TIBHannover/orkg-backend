package org.orkg.graph.adapter.input.rest.mapping

import java.util.*
import org.orkg.common.PageRequests
import org.orkg.graph.adapter.input.rest.ClassRepresentation
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases
import org.springframework.data.domain.Page

interface ClassRepresentationAdapter {
    val statementService: StatementUseCases

    fun Optional<Class>.mapToClassRepresentation(): Optional<ClassRepresentation> =
        map {
            it.toClassRepresentation(
                statementService.findAll(
                    pageable = PageRequests.SINGLE,
                    subjectId = it.id,
                    predicateId = Predicates.description,
                    objectClasses = setOf(Classes.literal)
                ).singleOrNull()?.`object`?.label
            )
        }

    fun Page<Class>.mapToClassRepresentation(): Page<ClassRepresentation> {
        val descriptions = when {
            content.isNotEmpty() -> {
                val ids = content.mapTo(mutableSetOf()) { it.id }
                statementService.findAllDescriptions(ids)
            }
            else -> emptyMap()
        }
        return map { it.toClassRepresentation(descriptions[it.id]) }
    }

    fun Class.toClassRepresentation(
        description: String?
    ): ClassRepresentation =
        ClassRepresentation(id, label, uri, description, createdAt, createdBy, modifiable)
}
