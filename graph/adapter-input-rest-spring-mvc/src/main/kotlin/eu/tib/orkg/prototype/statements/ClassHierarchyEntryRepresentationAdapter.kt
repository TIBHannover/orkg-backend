package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.RetrieveClassHierarchyUseCase.ClassHierarchyEntryRepresentation
import eu.tib.orkg.prototype.statements.spi.ClassHierarchyRepository.ClassHierarchyEntry
import org.springframework.data.domain.Page

interface ClassHierarchyEntryRepresentationAdapter : ClassRepresentationAdapter {

    fun Page<ClassHierarchyEntry>.mapToClassHierarchyEntryRepresentation(): Page<ClassHierarchyEntryRepresentation> =
        map { it.toClassHierarchyEntryRepresentation() }

    private fun ClassHierarchyEntry.toClassHierarchyEntryRepresentation(): ClassHierarchyEntryRepresentation =
        ClassHierarchyEntryRepresentation(`class`.toClassRepresentation(), parentId)
}
