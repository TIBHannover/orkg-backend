package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.contenttypes.domain.model.Visibility
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResearchProblemRepository {

    fun findById(id: ThingId): Optional<Resource>

    fun findAllListedContributionsByProblem(id: ThingId, pageable: Pageable): Page<Resource>
    fun findAllContributionsByProblemAndVisibility(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>

    fun findAllListedPapersByProblem(id: ThingId, pageable: Pageable): Page<Resource>
    fun findAllPapersByProblemAndVisibility(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>

    fun findAllListedResearchFieldsByProblem(id: ThingId, pageable: Pageable): Page<Resource>
    fun findAllResearchFieldsByProblemAndVisibility(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>

    fun findAllListedComparisonsByProblem(id: ThingId, pageable: Pageable): Page<Resource>
    fun findAllComparisonsByProblemAndVisibility(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>

    fun findAllListedLiteratureListsByProblem(id: ThingId, pageable: Pageable): Page<Resource>
    fun findAllLiteratureListsByProblemAndVisibility(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>

    fun findAllListedSmartReviewsByProblem(id: ThingId, pageable: Pageable): Page<Resource>
    fun findAllSmartReviewsByProblemAndVisibility(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>

    fun findAllListedVisualizationsByProblem(id: ThingId, pageable: Pageable): Page<Resource>
    fun findAllVisualizationsByProblemAndVisibility(id: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>

    fun findResearchFieldsPerProblem(problemId: ThingId): Iterable<FieldPerProblem>
    fun findTopResearchProblemsGoingBack(months: Int): Iterable<Resource>
    fun findTopResearchProblemsAllTime(): Iterable<Resource>
    fun findContributorsLeaderboardPerProblem(problemId: ThingId, pageable: Pageable): Page<ContributorPerProblem>
    fun findResearchProblemForDataset(datasetId: ThingId, pageable: Pageable): Page<Resource>
    fun findAllListedProblems(pageable: Pageable): Page<Resource>
    fun findAllProblemsByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource>

    data class FieldPerProblem(
        val field: Resource,
        val freq: Long
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
