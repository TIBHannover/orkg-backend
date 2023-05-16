package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.statements.domain.model.ClassSubclassRelation
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface ClassRelationRepository {
    fun save(classRelation: ClassSubclassRelation)
    fun saveAll(classRelations: Set<ClassSubclassRelation>)
    fun deleteByChildClassId(childId: ThingId)
    fun deleteAll()
}
