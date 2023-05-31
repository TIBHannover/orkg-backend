package eu.tib.orkg.prototype.statements

import eu.tib.orkg.prototype.statements.api.ChildClassRepresentation
import eu.tib.orkg.prototype.statements.api.RetrieveClassHierarchyUseCase
import org.springframework.data.domain.Page

interface ChildClassRepresentationAdapter : ClassRepresentationAdapter {

    fun Page<RetrieveClassHierarchyUseCase.ChildClass>.mapToChildClassRepresentation(): Page<ChildClassRepresentation> =
        map { it.toChildClassRepresentation() }

    fun RetrieveClassHierarchyUseCase.ChildClass.toChildClassRepresentation(): ChildClassRepresentation =
        ChildClassRepresentation(`class`.toClassRepresentation(), childCount)
}
