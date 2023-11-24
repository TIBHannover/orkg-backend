package org.orkg.graph.adapter.input.rest.mapping

import org.orkg.graph.domain.ClassHierarchyEntry
import org.orkg.graph.input.ClassHierarchyEntryRepresentation
import org.springframework.data.domain.Page

interface ClassHierarchyEntryRepresentationAdapter : ClassRepresentationAdapter {

    fun Page<ClassHierarchyEntry>.mapToClassHierarchyEntryRepresentation(): Page<ClassHierarchyEntryRepresentation> =
        map { it.toClassHierarchyEntryRepresentation() }

    fun ClassHierarchyEntry.toClassHierarchyEntryRepresentation(): ClassHierarchyEntryRepresentation =
        ClassHierarchyEntryRepresentation(`class`.toClassRepresentation(), parentId)
}
