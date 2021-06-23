package eu.tib.orkg.prototype.statements.domain.model

import eu.tib.orkg.prototype.statements.application.port.`in`.MarkAsUnlistedService
import eu.tib.orkg.prototype.statements.application.port.`in`.MarkFeaturedService
import eu.tib.orkg.prototype.statements.application.port.out.GetProblemFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.LoadProblemAdapter
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ContributorPerProblem
import java.util.Optional
import org.springframework.data.domain.Pageable

interface ProblemService :
    GetProblemFlagQuery,
    LoadProblemAdapter,
    MarkFeaturedService,
    MarkAsUnlistedService {
    fun findById(id: ResourceId): Optional<Resource>

    fun findFieldsPerProblem(problemId: ResourceId): List<Any>

    fun findTopResearchProblems(): List<Resource>

    fun findContributorsPerProblem(problemId: ResourceId, pageable: Pageable): List<ContributorPerProblem>

    fun findAuthorsPerProblem(problemId: ResourceId, pageable: Pageable): List<Any>
}
