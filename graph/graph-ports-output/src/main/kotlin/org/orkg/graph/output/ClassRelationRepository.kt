package org.orkg.graph.output

import org.orkg.common.ThingId
import org.orkg.graph.domain.ClassSubclassRelation

interface ClassRelationRepository {
    fun save(classRelation: ClassSubclassRelation)

    fun saveAll(classRelations: Set<ClassSubclassRelation>)

    fun deleteByChildId(childId: ThingId)

    fun deleteAll()
}
