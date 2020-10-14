package eu.tib.orkg.prototype.statements.domain.model

interface ProblemService {

    fun getFieldsPerProblem(problemId: ResourceId): List<Any>

    fun getTopResearchProblems(): List<Resource>
}
