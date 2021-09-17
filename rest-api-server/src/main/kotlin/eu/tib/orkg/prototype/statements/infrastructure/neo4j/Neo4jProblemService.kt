package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveResearchProblemsUseCase
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.domain.model.ProblemService
import eu.tib.orkg.prototype.statements.domain.model.ProblemService.ContributionStatistics
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.ports.AuthorPerProblem
import eu.tib.orkg.prototype.statements.ports.ContributorRepository
import eu.tib.orkg.prototype.statements.ports.ProblemRepository
import eu.tib.orkg.prototype.statements.ports.ResourceRepository
import java.util.Optional
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jProblemService(
    private val problemRepository: ProblemRepository,
    private val resourceRepository: ResourceRepository,
    private val contributorRepository: ContributorRepository
) : ProblemService, RetrieveResearchProblemsUseCase {
    override fun findById(id: ResourceId): Optional<Resource> = problemRepository.findById(id)

    override fun findFieldsPerProblem(problemId: ResourceId): List<Any> =
        problemRepository.findResearchFieldsPerProblem(problemId)

    override fun findTopResearchProblems(): List<Resource> =
        problemRepository.findTopResearchProblems()

    override fun findContributorsPerProblem(problemId: ResourceId, pageable: Pageable): List<ContributionStatistics> =
        problemRepository
            .findContributorsLeaderboardPerProblem(problemId, pageable)
            .content
            .map {
                ContributionStatistics(
                    user = contributorRepository.findById(ContributorId(it.contributor)).get(),
                    contributions = it.freq
                )
            }

    override fun findAuthorsPerProblem(problemId: ResourceId, pageable: Pageable): List<Any> {
        return problemRepository.findAuthorsLeaderboardPerProblem(problemId, pageable)
            .content
            .map(AuthorPerProblem::toJsonObjects)
    }

    override fun forDataset(id: ResourceId): Optional<List<ResearchProblem>> {
        val dataset = resourceRepository.findById(id)
        if (!dataset.isPresent) return Optional.empty()
        return Optional.of(problemRepository
            .findResearchProblemForDataset(id)
            .map { ResearchProblem(it.id!!, it.label) })
    }
}
