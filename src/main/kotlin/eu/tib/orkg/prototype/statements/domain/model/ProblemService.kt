package eu.tib.orkg.prototype.statements.domain.model

import java.util.Optional

interface ProblemService {
    fun findById(id: ResourceId): Optional<Resource>

    fun getFieldsPerProblem(problemId: ResourceId): List<Any>

    fun getTopResearchProblems(): List<Resource>
}
