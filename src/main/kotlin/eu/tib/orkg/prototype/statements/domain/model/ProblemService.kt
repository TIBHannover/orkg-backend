package eu.tib.orkg.prototype.statements.domain.model

import java.util.Optional
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ContributorPerProblem
import org.springframework.data.domain.Pageable

interface ProblemService {
    fun findById(id: ResourceId): Optional<Resource>

    fun findFieldsPerProblem(problemId: ResourceId): List<Any>

    fun findTopResearchProblems(): List<Resource>

    fun findContributorsPerProblem(problemId: ResourceId, pageable: Pageable): List<ContributorPerProblem>

    fun findAuthorsPerProblem(problemId: ResourceId, pageable: Pageable): List<Any>
}
