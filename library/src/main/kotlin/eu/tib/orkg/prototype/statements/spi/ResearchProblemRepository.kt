package eu.tib.orkg.prototype.statements.spi

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResearchProblemRepository {

    fun findById(id: ThingId): Optional<Resource>
    fun findContributionsByProblems(
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>
    fun findContributionsByProblems(
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>
    fun findPapersByProblems(
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>
    fun findPapersByProblems(
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>
    fun findResearchFieldsByProblems(
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>
    fun findResearchFieldsByProblems(
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>
    fun findComparisonsByProblems(
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>
    fun findComparisonsByProblems(
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>
    fun findLiteratureListsByProblems(
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>
    fun findLiteratureListsByProblems(
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>
    fun findSmartReviewsByProblems(
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>
    fun findSmartReviewsByProblems(
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>
    fun findVisualizationsByProblems(
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>
    fun findVisualizationsByProblems(
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<DetailsPerProblem>
    fun findResearchFieldsPerProblem(problemId: ThingId): Iterable<FieldPerProblem>
    fun findTopResearchProblemsGoingBack(months: Int): Iterable<Resource>
    fun findTopResearchProblemsAllTime(): Iterable<Resource>
    fun findContributorsLeaderboardPerProblem(problemId: ThingId, pageable: Pageable): Page<ContributorPerProblem>
    fun findResearchProblemForDataset(datasetId: ThingId, pageable: Pageable): Page<Resource>
    fun findAllFeaturedProblems(pageable: Pageable): Page<Resource>
    fun findAllNonFeaturedProblems(pageable: Pageable): Page<Resource>
    fun findAllUnlistedProblems(pageable: Pageable): Page<Resource>
    fun findAllListedProblems(pageable: Pageable): Page<Resource>

    data class FieldPerProblem(
        val field: Resource,
        val freq: Long
    )

    data class DetailsPerProblem(
        val id: ThingId?,
        val label: String?,
        @JsonProperty("created_at")
        val createdAt: String?,
        val featured: Boolean?,
        val unlisted: Boolean?,
        val classes: List<String>,
        @JsonProperty("created_by")
        val createdBy: String?
    )

    data class ContributorPerProblem(
        val user: String,
        val freq: Long
    ) {
        val contributor: UUID = UUID.fromString(user)
        val isAnonymous: Boolean
            get() = contributor == UUID(0, 0)
    }

    data class AuthorPerProblem(
        val author: String,
        val thing: Thing,
        val papers: Long
    ) {
        val isLiteral: Boolean
            get() = thing is Literal
        val toAuthorResource: Resource
            get() = thing as Resource
    }
}
