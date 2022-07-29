package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.domain.model.neo4j.ContributorPerProblem
import eu.tib.orkg.prototype.statements.domain.model.neo4j.DetailsPerProblem
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProblemService {
    fun findById(id: ResourceId): Optional<Resource>

    fun findFieldsPerProblem(problemId: ResourceId): List<Any>

    fun findFieldsPerProblemAndClasses(
        problemId: ResourceId,
        featured: Boolean?,
        unlisted: Boolean,
        classes: List<String>,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    fun findTopResearchProblems(): List<Resource>

    fun findContributorsPerProblem(problemId: ResourceId, pageable: Pageable): List<ContributorPerProblem>

    fun findAuthorsPerProblem(problemId: ResourceId, pageable: Pageable): List<Any>
}
