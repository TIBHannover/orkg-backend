package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.application.port.out.GetProblemFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.LoadProblemPort
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResearchProblemRepository.ContributorPerProblem
import java.util.*
import org.codehaus.jackson.annotate.JsonProperty
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveResearchProblemUseCase :
    GetProblemFlagQuery,
    LoadProblemPort {
    fun findById(id: ThingId): Optional<Resource>

    fun findFieldsPerProblem(problemId: ThingId): List<FieldWithFreq>

    fun findAllEntitiesBasedOnClassByProblem(
        problemId: ThingId,
        classes: List<String>,
        visibilityFilter: VisibilityFilter,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    fun findTopResearchProblems(): List<Resource>

    fun findContributorsPerProblem(problemId: ThingId, pageable: Pageable): List<ContributorPerProblem>

    fun forDataset(id: ThingId, pageable: Pageable): Optional<Page<ResearchProblem>>

    data class FieldWithFreq(
        val field: Resource,
        val freq: Long,
    )

    data class DetailsPerProblem(
        val id: ThingId?,
        val label: String?,
        @JsonProperty("created_at")
        val createdAt: String?,
        val featured: Boolean?,
        val unlisted: Boolean?,
        val classes: List<String>,
        @JsonProperty("created_by")
        val createdBy: String?
    )
}
