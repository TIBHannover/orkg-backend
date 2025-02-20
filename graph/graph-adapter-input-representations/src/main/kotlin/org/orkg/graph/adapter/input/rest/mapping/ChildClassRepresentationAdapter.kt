package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.graph.adapter.input.rest.ChildClassRepresentation
import org.orkg.graph.domain.ChildClass
import org.springframework.data.domain.Page

interface ChildClassRepresentationAdapter : ClassRepresentationAdapter {
    fun Page<ChildClass>.mapToChildClassRepresentation(): Page<ChildClassRepresentation> {
        val descriptions = when {
            content.isNotEmpty() -> {
                val ids = content.mapTo(mutableSetOf()) { it.`class`.id }
                statementService.findAllDescriptionsById(ids)
            }
            else -> emptyMap()
        }
        return map { it.toChildClassRepresentation(descriptions[it.`class`.id]) }
    }

    fun ChildClass.toChildClassRepresentation(
        description: String?,
    ): ChildClassRepresentation =
        ChildClassRepresentation(`class`.toClassRepresentation(description), childCount)
}
