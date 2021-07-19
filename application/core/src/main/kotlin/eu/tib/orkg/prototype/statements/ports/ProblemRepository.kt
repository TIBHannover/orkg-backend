package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

interface ProblemRepository {

    fun findById(id: ResourceId): Optional<Resource>

    fun findFieldsPerProblem(problemId: ResourceId): List<Any>

    fun findTopResearchProblems(): List<Resource>

    //Do not know where to import and store ContributorPerProblem.
    // So commented this function temporarily
    //fun findContributorsPerProblem(problemId: ResourceId, pageable: Pageable): List<ContributorPerProblem>

    fun findAuthorsPerProblem(problemId: ResourceId, pageable: Pageable): List<Any>
}
