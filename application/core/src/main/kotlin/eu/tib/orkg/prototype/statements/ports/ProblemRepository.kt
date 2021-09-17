package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Thing
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

interface ProblemRepository {

    fun findById(id: ResourceId): Optional<Resource>

    fun findResearchFieldsPerProblem(problemId: ResourceId): List<FieldPerProblem>

    fun findTopResearchProblems(): List<Resource>

    fun findTopResearchProblemsAllTime(): List<Resource>

    fun findTopResearchProblemsGoingBack(months: Int): Iterable<Resource>

    fun findContributorsLeaderboardPerProblem(problemId: ResourceId, pageable: Pageable): Page<ContributorPerProblem>

    fun findAuthorsLeaderboardPerProblem(problemId: ResourceId, pageable: Pageable): Page<AuthorPerProblem>

    fun findResearchProblemForDataset(datasetId: ResourceId): Iterable<Resource>

    fun findAuthorsPerProblem(problemId: ResourceId, pageable: Pageable): List<AuthorPerProblem>

    data class ContributorPerProblem(
        val user: String,
        val freq: Long
    ) {
        val contributor: UUID = UUID.fromString(user)
        val isAnonymous: Boolean
            get() = contributor == UUID(0, 0)
    }
}

data class FieldPerProblem(
    val field: Resource,
    val freq: Long
)

data class AuthorPerProblem(
    val author: String,
    val thing: Thing,
    val papers: Long
) {
    val isLiteral: Boolean
        get() = thing is Literal

    val toAuthorResource: Resource
        get() = thing as Resource

    // This is a hack and needs a proper "author" abstraction.
    fun toJsonObjects(): Any {
        return if (this.isLiteral)
            object : AuthorAsJson {
                override val author: String by lazy { author }
                override val papers: Long by lazy { papers }
            }
        else
            object : AuthorAsJson {
                override val author: Resource by lazy { thing as Resource }
                override val papers: Long by lazy { papers }
            }
    }

    interface AuthorAsJson {
        val author: Any
        val papers: Long
    }
}
