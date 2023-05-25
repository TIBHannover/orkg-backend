package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.RetrieveClassHierarchyUseCase
import eu.tib.orkg.prototype.statements.spi.ClassHierarchyRepository
import org.springframework.data.domain.Page

interface ChildClassRepresentationAdapter : ClassRepresentationAdapter {

    fun Page<ClassHierarchyRepository.ChildClass>.mapToChildClassRepresentation(): Page<RetrieveClassHierarchyUseCase.ChildClassRepresentation> =
        map { it.toChildClassRepresentation() }

    private fun ClassHierarchyRepository.ChildClass.toChildClassRepresentation(): RetrieveClassHierarchyUseCase.ChildClassRepresentation =
        RetrieveClassHierarchyUseCase.ChildClassRepresentation(`class`.toClassRepresentation(), childCount)
}
