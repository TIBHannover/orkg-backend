package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.domain.model.neo4j.ContributorPerProblem
import org.springframework.data.domain.Pageable

interface ProblemService {

    fun getFieldsPerProblem(problemId: ResourceId): List<Any>

    fun getTopResearchProblems(): List<Resource>

    fun getContributorsPerProblem(problemId: ResourceId, pageable: Pageable): List<ContributorPerProblem>

    fun getAuthorsPerProblem(problemId: ResourceId, pageable: Pageable): List<Any>
}
