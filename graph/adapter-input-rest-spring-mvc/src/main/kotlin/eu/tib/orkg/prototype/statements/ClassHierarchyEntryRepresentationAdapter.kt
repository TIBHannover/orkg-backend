package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.RetrieveClassHierarchyUseCase.*
import org.springframework.data.domain.Page

interface ClassHierarchyEntryRepresentationAdapter : ClassRepresentationAdapter {

    fun Page<ClassHierarchyEntry>.mapToClassHierarchyEntryRepresentation(): Page<ClassHierarchyEntryRepresentation> =
        map { it.toClassHierarchyEntryRepresentation() }

    fun ClassHierarchyEntry.toClassHierarchyEntryRepresentation(): ClassHierarchyEntryRepresentation =
        ClassHierarchyEntryRepresentation(`class`.toClassRepresentation(), parentId)
}
