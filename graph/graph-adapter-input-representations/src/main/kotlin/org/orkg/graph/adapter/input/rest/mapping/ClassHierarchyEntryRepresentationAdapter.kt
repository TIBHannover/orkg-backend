package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.graph.domain.ClassHierarchyEntry
import org.orkg.graph.adapter.input.rest.ClassHierarchyEntryRepresentation
import org.springframework.data.domain.Page

interface ClassHierarchyEntryRepresentationAdapter : ClassRepresentationAdapter {

    fun Page<ClassHierarchyEntry>.mapToClassHierarchyEntryRepresentation(): Page<ClassHierarchyEntryRepresentation> {
        val descriptions = when {
            content.isNotEmpty() -> {
                val ids = content.mapTo(mutableSetOf()) { it.`class`.id }
                statementService.findAllDescriptionsById(ids)
            }
            else -> emptyMap()
        }
        return map { it.toClassHierarchyEntryRepresentation(descriptions[it.`class`.id]) }
    }

    fun ClassHierarchyEntry.toClassHierarchyEntryRepresentation(
        description: String?
    ): ClassHierarchyEntryRepresentation =
        ClassHierarchyEntryRepresentation(`class`.toClassRepresentation(description), parentId)
}
