package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import java.util.Optional
import java.util.UUID
import org.springframework.data.domain.Pageable

interface ProblemService {
    fun findById(id: ResourceId): Optional<Resource>

    fun findFieldsPerProblem(problemId: ResourceId): List<Any>

    fun findTopResearchProblems(): List<Resource>

    fun findContributorsPerProblem(problemId: ResourceId, pageable: Pageable): List<ContributionStatistics>

    fun findAuthorsPerProblem(problemId: ResourceId, pageable: Pageable): List<Any>

    data class ContributionStatistics(
        val user: Contributor,
        val contributions: Long
    ) {
        // TODO: is this used in JSON?
        val isAnonymous: Boolean
            get() = user.id.value == UUID(0, 0)
    }
}
