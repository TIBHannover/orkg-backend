package org.orkg.graph.adapter.output.inmemory

import org.orkg.common.ThingId
import org.orkg.graph.domain.ClassSubclassRelation
import org.orkg.graph.output.ClassRelationRepository

class InMemoryClassRelationRepository(
    private val inMemoryGraph: InMemoryGraph
) : ClassRelationRepository {
    override fun save(classRelation: ClassSubclassRelation) =
        inMemoryGraph.add(classRelation)

    override fun saveAll(classRelations: Set<ClassSubclassRelation>) =
        classRelations.forEach(inMemoryGraph::add)

    override fun deleteByChildId(childId: ThingId) =
        inMemoryGraph.findClassRelationByChildId(childId).ifPresent(inMemoryGraph::remove)

    override fun deleteAll() =
        inMemoryGraph.removeAllClassRelations()
}
