package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.id
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.limit
import eu.tib.orkg.prototype.core.statements.adapters.output.eu.tib.orkg.prototype.statements.domain.model.neo4j.skip
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jProblemRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jThingRepository
import eu.tib.orkg.prototype.statements.ports.AuthorPerProblem
import eu.tib.orkg.prototype.statements.ports.FieldPerProblem
import eu.tib.orkg.prototype.statements.ports.ProblemRepository
import eu.tib.orkg.prototype.statements.ports.ProblemRepository.ContributorPerProblem
import org.neo4j.driver.Record
import org.neo4j.driver.types.TypeSystem
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class ProblemPersistenceAdapter(
    private val neo4jProblemRepository: Neo4jProblemRepository,
    private val neo4jThingRepository: Neo4jThingRepository,
    private val client: Neo4jClient
): ProblemRepository {
    override fun findById(id: ResourceId): Optional<Resource> =
        neo4jProblemRepository
            .findById(id)
            .map(Neo4jResource::toResource)

    override fun findTopResearchProblems(): List<Resource> =
        findTopResearchProblemsGoingBack(listOf(1, 2, 3, 6), emptyList())
            .map(Neo4jResource::toResource)

    override fun findTopResearchProblemsAllTime(): List<Resource> =
        neo4jProblemRepository.findTopResearchProblemsAllTime().map(Neo4jResource::toResource)

    override fun findTopResearchProblemsGoingBack(months: Int): Iterable<Resource> =
        neo4jProblemRepository.findTopResearchProblemsGoingBack(months).map(Neo4jResource::toResource)

    override fun findContributorsLeaderboardPerProblem(
        problemId: ResourceId,
        pageable: Pageable
    ): Page<ContributorPerProblem> {
        val countQuery =
            """MATCH (problem:Problem {resource_id: ${id}})<-[:RELATED {predicate_id: 'P32'}]-(contribution:Contribution)
                    WHERE contribution.created_by IS NOT NULL AND contribution.created_by <> '00000000-0000-0000-0000-000000000000'
                    WITH contribution.created_by AS user, COUNT(contribution.created_by) AS freq
                    RETURN COUNT(user)"""
        val total = client.query(countQuery).bind(problemId).to("id").fetchAs<Long>().one() ?: 0
        // if (total <= 0) return Page.empty()
        val resultQuery = """
            MATCH (problem:Problem {resource_id: ${id}})<-[:RELATED {predicate_id: 'P32'}]-(contribution:Contribution)
            WHERE contribution.created_by IS NOT NULL AND contribution.created_by <> '00000000-0000-0000-0000-000000000000'
            RETURN contribution.created_by AS user, COUNT(contribution.created_by) AS freq
            ORDER BY freq DESC SKIP $skip LIMIT $limit"""
        val result = client.query(resultQuery)
            .bind(problemId).to("id")
            .fetchAs<ContributorPerProblem>()
            .mappedBy { _: TypeSystem, record: Record ->
                ContributorPerProblem(
                    user = record["user"].asString(),
                    freq = record["freq"].asLong(0),
                )
            }
            .all()
            .toList()
        return PageImpl(result, pageable, total)
    }

    override fun findAuthorsLeaderboardPerProblem(problemId: ResourceId, pageable: Pageable): Page<AuthorPerProblem> {
        // TODO: Should group on the resource and not on the label. See https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/172#note_378465870
        val countQuery = """
            MATCH (problem:Problem {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P27'}]->(author: Thing)
            WITH author.label AS author, COLLECT(author)[0] AS thing , COUNT(paper.resource_id) AS papers
            RETURN COUNT (author)
            """
        val total = client.query(countQuery).bind(problemId).to("id").fetchAs<Long>().one() ?: 0
        val resultQuery = """
            MATCH (problem:Problem {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P27'}]->(author: Thing)
            RETURN author.label AS author, COLLECT(author)[0] AS thing , COUNT(paper.resource_id) AS papers
            ORDER BY papers DESC, author SKIP $skip LIMIT $limit
            """
        val result = client.query(resultQuery)
            .bind(problemId).to("id")
            .fetchAs<AuthorPerProblem>()
            .mappedBy(authorPerProblemMapper)
            .all()
            .toList()
        return PageImpl(result, pageable, total)
    }

    override fun findResearchProblemForDataset(datasetId: ResourceId): Iterable<Resource> =
        neo4jProblemRepository.findResearchProblemForDataset(datasetId).map(Neo4jResource::toResource)

    /*
    Iterate over the list of months, and if no problems are found go back a bit more in time
    and if none found take all time results
     */
    private fun findTopResearchProblemsGoingBack(
        listOfMonths: List<Int>,
        result: List<Neo4jResource>
    ): Iterable<Neo4jResource> {
        val month = listOfMonths.firstOrNull()
        val problems = if (month == null)
            neo4jProblemRepository.findTopResearchProblemsAllTime()
        else
            neo4jProblemRepository.findTopResearchProblemsGoingBack(month)
        val newResult = result.plus(problems).distinct()
        return if (newResult.count() >= 5)
            newResult.take(5)
        else
            findTopResearchProblemsGoingBack(listOfMonths.drop(1), newResult)
    }

    override fun findAuthorsPerProblem(problemId: ResourceId, pageable: Pageable): List<AuthorPerProblem> {
        // TODO: Should group on the resource and not on the label. See https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/172#note_378465870
        val resultQuery = """
            MATCH (problem:Problem {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P27'}]->(author: Thing)
            RETURN author.label AS author, collect(author)[0].id AS thing, COUNT(paper.resource_id) AS papers
            ORDER BY papers DESC, author SKIP $skip LIMIT $limit
            """
        /* Unused, because it does not return a page.
        val countQuery = """
            MATCH (problem:Problem {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P27'}]->(author: Thing)
            WITH author.label AS author, collect(author)[0].id AS thing, COUNT(paper.resource_id) AS papers
            RETURN COUNT (author)
            """
        val total = client.query(countQuery).fetchAs<Long>().one()
        */
        // FIXME: should return a page
        return client
            .query(resultQuery)
            .bind(problemId).to("id")
            .fetchAs<AuthorPerProblem>()
            .mappedBy(authorPerProblemMapper)
            .all()
            .toList()
    }

    override fun findResearchFieldsPerProblem(problemId: ResourceId): List<FieldPerProblem> {
        val query = """
            MATCH (:Problem {resource_id: $id})<-[:RELATED {predicate_id: 'P32'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(field:ResearchField)
            RETURN field.resource_id AS field, COUNT(paper) AS freq
            ORDER BY freq DESC
            """
        val result = client.query(query)
            .bind(problemId.value).to("id")
            .fetchAs<FieldPerProblem>()
            .mappedBy { _: TypeSystem, record: Record ->
                val id = ResourceId(record["field"].asString())
                val resource = neo4jProblemRepository
                    .findById(id)
                    .orElseThrow { IllegalStateException("Resource $id was deleted between queries. This should not happen!") }
                    .toResource()
                FieldPerProblem(
                    field = resource,
                    freq = record["freq"].asLong()
                )
            }
            .all()
            .toList()
        return result
    }

    private val authorPerProblemMapper: (ts: TypeSystem, record: Record) -> AuthorPerProblem =
        { _: TypeSystem, record: Record ->
            // TODO: fix n+1 issue
            // TODO: move to author service, or some other component more fitting (TBD)
            val thingId = record["thing"].asString()
            val thing = neo4jThingRepository
                .findByThingId(thingId)
                .orElseThrow { IllegalStateException("Thing $thingId was deleted between queries. This should not happen!") }
                .toThing()
            AuthorPerProblem(
                author = record["author"].asString(),
                thing = thing,
                papers = record["papers"].asLong()
            )
        }
}
