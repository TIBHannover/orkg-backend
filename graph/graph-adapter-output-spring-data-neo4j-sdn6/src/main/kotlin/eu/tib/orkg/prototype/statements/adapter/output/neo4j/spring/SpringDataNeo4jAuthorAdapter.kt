package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jAuthorOfComparison
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jAuthorPerProblem
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jAuthorRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jLiteral
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.Author
import eu.tib.orkg.prototype.statements.domain.model.ComparisonAuthor
import eu.tib.orkg.prototype.statements.domain.model.ComparisonAuthorInfo
import eu.tib.orkg.prototype.statements.domain.model.PaperAuthor
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.AuthorRepository
import java.lang.IllegalStateException
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
        when(authorResource) {
            // TODO: Replace with proper abstraction of Author
            is Neo4jResource -> Author.ResourceAuthor(authorResource.toResource())
            is Neo4jLiteral -> Author.LiteralAuthor(authorResource.label!!)
            else -> throw IllegalStateException()
        },
        info.map { ComparisonAuthorInfo(paperId = ThingId(it.paper), it.index.toInt(), it.year?.toInt()) }
    )

private fun Neo4jAuthorPerProblem.toPaperAuthor() =
    PaperAuthor(
        if (thing is Neo4jResource)
            Author.ResourceAuthor(thing.toResource())
        else
            Author.LiteralAuthor(thing.label!!),
        papers.toInt()
    )