package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring


import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPaperRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPaperWithPath
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jPredicate
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jThing
import eu.tib.orkg.prototype.statements.domain.model.PaperResourceWithPath
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.spi.PaperRepository
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
