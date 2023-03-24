package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.application.port.out.GetProblemFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.LoadProblemPort
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResearchProblemRepository.ContributorPerProblem
import eu.tib.orkg.prototype.statements.spi.ResearchProblemRepository.DetailsPerProblem
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveResearchProblemUseCase :
    GetProblemFlagQuery,
    LoadProblemPort {
    fun findById(id: ThingId): Optional<ResourceRepresentation>

    fun findFieldsPerProblem(problemId: ThingId): List<FieldCount>

    fun findFieldsPerProblemAndClasses(
        problemId: ThingId,
        featured: Boolean?,
        unlisted: Boolean,
        classes: List<String>,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    fun findTopResearchProblems(): List<ResourceRepresentation>

    fun findContributorsPerProblem(problemId: ThingId, pageable: Pageable): List<ContributorPerProblem>

    fun forDataset(id: ThingId, pageable: Pageable): Optional<Page<ResearchProblem>>

    data class FieldCount(
        val field: ResourceRepresentation,
        val freq: Long,
    )
}
