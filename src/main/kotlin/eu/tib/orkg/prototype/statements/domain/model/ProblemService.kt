package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.domain.model.neo4j.ContributorPerProblem

interface ProblemService {

    fun getFieldsPerProblem(problemId: ResourceId): List<Any>

    fun getContributorsPerProblem(problemId: ResourceId): List<ContributorPerProblem>
}
