package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jPaperRepository
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jPaperWithPath
import org.orkg.contenttypes.output.PaperRepository
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jPredicate
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jThing
import org.orkg.graph.domain.PaperResourceWithPath
import org.orkg.graph.domain.Thing
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jPaperAdapter(
    private val neo4jRepository: Neo4jPaperRepository
) : PaperRepository {

    override fun findAllPapersRelatedToResource(id: ThingId, pageable: Pageable): Page<PaperResourceWithPath> =
        neo4jRepository.findAllPapersRelatedToResource(id, pageable)
            .map { it.toPaperResourceWithPath() }

}

fun Neo4jPaperWithPath.toPaperResourceWithPath() =
    PaperResourceWithPath(
        paper.toResource(),
        aggregateAndConvertToModelObjects(paper.id!!, path)
    )

private fun aggregateAndConvertToModelObjects(paperId: ThingId, path: Iterable<Neo4jThing>): List<List<Thing>> {
    val finalResult = mutableListOf<List<Thing>>()
    var possiblePath = mutableListOf<Thing>()
    for (p in path) {
        if (p.id!! == paperId) {
            possiblePath = mutableListOf()
            finalResult.add(possiblePath)
        }
        when (p) {
            is Neo4jResource, is Neo4jPredicate -> possiblePath.add(p.toThing())
            else -> throw IllegalStateException("Result types can only be either resources or predicates. This is a bug!")
        }
    }
    return finalResult
}
