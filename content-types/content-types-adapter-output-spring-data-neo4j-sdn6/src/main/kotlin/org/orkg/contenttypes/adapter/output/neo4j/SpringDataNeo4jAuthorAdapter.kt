package org.orkg.contenttypes.adapter.output.neo4j

import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jAuthorOfComparison
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jAuthorPerProblem
import org.orkg.contenttypes.adapter.output.neo4j.internal.Neo4jAuthorRepository
import org.orkg.contenttypes.domain.ComparisonAuthor
import org.orkg.contenttypes.domain.ComparisonAuthorInfo
import org.orkg.contenttypes.domain.PaperAuthor
import org.orkg.contenttypes.domain.SimpleAuthor
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jLiteral
import org.orkg.graph.adapter.output.neo4j.internal.Neo4jResource
import org.orkg.graph.output.AuthorRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jAuthorAdapter(
    private val neo4jRepository: Neo4jAuthorRepository
) : AuthorRepository {
    override fun findTopAuthorsOfComparison(id: ThingId, pageable: Pageable): Page<ComparisonAuthor> =
        neo4jRepository.findTopAuthorsOfComparison(id, pageable)
            .map(Neo4jAuthorOfComparison::toComparisonAuthor)

    override fun findAuthorsLeaderboardPerProblem(problemId: ThingId, pageable: Pageable): Page<PaperAuthor> =
        neo4jRepository.findAuthorsLeaderboardPerProblem(problemId, pageable)
            .map(Neo4jAuthorPerProblem::toPaperAuthor)
}

private fun Neo4jAuthorOfComparison.toComparisonAuthor() =
    ComparisonAuthor(
        when (authorResource) {
            // TODO: Replace with proper abstraction of Author
            is Neo4jResource -> SimpleAuthor.ResourceAuthor(authorResource.toResource())
            is Neo4jLiteral -> SimpleAuthor.LiteralAuthor(authorResource.label!!)
            else -> throw IllegalStateException()
        },
        info.map {
            ComparisonAuthorInfo(
                paperId = ThingId(it.paper),
                it.index.toInt(),
                it.year?.toInt()
            )
        }
    )

private fun Neo4jAuthorPerProblem.toPaperAuthor() =
    PaperAuthor(
        if (thing is Neo4jResource)
            SimpleAuthor.ResourceAuthor(thing.toResource())
        else
            SimpleAuthor.LiteralAuthor(thing.label!!),
        papers.toInt()
    )
