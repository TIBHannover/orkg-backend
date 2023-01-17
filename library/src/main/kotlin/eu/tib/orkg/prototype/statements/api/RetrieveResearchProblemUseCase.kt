package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.application.port.out.GetProblemFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.LoadProblemPort
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ContributorPerProblem
import eu.tib.orkg.prototype.statements.domain.model.neo4j.DetailsPerProblem
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface RetrieveResearchProblemUseCase :
    GetProblemFlagQuery,
    LoadProblemPort {
    fun findById(id: ResourceId): Optional<ResourceRepresentation>

    fun findFieldsPerProblem(problemId: ResourceId): List<FieldCount>

    fun findFieldsPerProblemAndClasses(
        problemId: ResourceId,
        featured: Boolean?,
        unlisted: Boolean,
        classes: List<String>,
        pageable: Pageable
    ): Page<DetailsPerProblem>

    fun findTopResearchProblems(): List<ResourceRepresentation>

    fun findContributorsPerProblem(problemId: ResourceId, pageable: Pageable): List<ContributorPerProblem>

    fun findAuthorsPerProblem(problemId: ResourceId, pageable: Pageable): List<PaperCountPerAuthor>

    fun forDataset(id: ResourceId): Optional<List<ResearchProblem>>

    data class FieldCount(
        val field: ResourceRepresentation,
        val freq: Long,
    )

    data class PaperCountPerAuthor(
        val author: Any, // TODO: Provide a proper abstraction
        val papers: Long,
    )
}
