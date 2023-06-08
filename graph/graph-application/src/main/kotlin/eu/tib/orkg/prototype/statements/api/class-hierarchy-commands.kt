package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface CreateClassHierarchyUseCase {
    fun create(userId: ContributorId, parentId: ThingId, childIds: Set<ThingId>, checkIfParentIsLeaf: Boolean)
}

interface DeleteClassHierarchyUseCase {
    fun delete(childId: ThingId)
}
