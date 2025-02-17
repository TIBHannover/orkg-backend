package org.orkg.graph.input

import org.orkg.common.ContributorId
import org.orkg.common.ThingId

interface CreateClassHierarchyUseCase {
    fun create(userId: ContributorId, parentId: ThingId, childIds: Set<ThingId>, checkIfParentIsLeaf: Boolean)
}

interface DeleteClassHierarchyUseCase {
    fun deleteByChildId(childId: ThingId)
}
