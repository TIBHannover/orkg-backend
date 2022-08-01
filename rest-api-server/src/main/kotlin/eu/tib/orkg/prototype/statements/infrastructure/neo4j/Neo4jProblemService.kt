package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.paperswithcode.application.port.input.RetrieveResearchProblemsUseCase
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.domain.model.ProblemService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ContributorPerProblem
import eu.tib.orkg.prototype.statements.domain.model.neo4j.DetailsPerProblem
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jProblemRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jProblemService(
    private val neo4jProblemRepository: Neo4jProblemRepository,
    private val resourceService: ResourceUseCases
) : ProblemService, RetrieveResearchProblemsUseCase {
override fun findById(id: ResourceId): Optional<Resource> =
        neo4jProblemRepository
            .findById(id)
            .map(Neo4jResource::toResource)

    override fun findFieldsPerProblem(problemId: ResourceId): List<Any> {
        return neo4jProblemRepository.findResearchFieldsPerProblem(problemId).map {
            object {
                val field = it.field.toResource()
                val freq = it.freq
            }
        }
    }

    override fun findFieldsPerProblemAndClasses(
        problemId: ResourceId,
        featured: Boolean?,
        unlisted: Boolean,
        classes: List<String>,
        pageable: Pageable
    ): Page<DetailsPerProblem> {
        val resultList = mutableListOf<DetailsPerProblem>()
        if (classes.isNotEmpty()) {
            when (featured) {
                null -> getProblemsWithoutFeatured(classes, problemId, unlisted, pageable, resultList)
                else -> getProblemsWithFeatured(classes, problemId, featured, unlisted, pageable, resultList)
            }
            resultList.sortBy(DetailsPerProblem::createdAt)
        } else {
            return Page.empty()
        }

        return PageImpl(resultList as List<DetailsPerProblem>, pageable, resultList.size.toLong())
    }

    override fun findTopResearchProblems(): List<Resource> =
        findTopResearchProblemsGoingBack(listOf(1, 2, 3, 6), emptyList())
            .map(Neo4jResource::toResource)

    /*
    Iterate over the list of months, and if no problems are found go back a bit more in time
    and if none found take all time results
     */
    private fun findTopResearchProblemsGoingBack(listOfMonths: List<Int>, result: List<Neo4jResource>): Iterable<Neo4jResource> {
        val month = listOfMonths.firstOrNull()
        val problems = if (month == null)
            neo4jProblemRepository.findTopResearchProblemsAllTime()
        else
            neo4jProblemRepository.findTopResearchProblemsGoingBack(month)
        val newResult = result.plus(problems).distinct()
        return if (newResult.count() >= 5)
            newResult.take(5)
        else
            findTopResearchProblemsGoingBack(listOfMonths.drop(1), newResult)
    }

    override fun findContributorsPerProblem(problemId: ResourceId, pageable: Pageable): List<ContributorPerProblem> {
        return neo4jProblemRepository
            .findContributorsLeaderboardPerProblem(problemId, pageable)
            .content
    }

    override fun findAuthorsPerProblem(problemId: ResourceId, pageable: Pageable): List<Any> {
        return neo4jProblemRepository.findAuthorsLeaderboardPerProblem(problemId, pageable)
            .content
            .map {
                if (it.isLiteral)
                    object {
                        val author = it.author
                        val papers = it.papers
                    }
                else
                    object {
                        val author = it.toAuthorResource.toResource()
                        val papers = it.papers
                    }
            }
    }

    override fun forDataset(id: ResourceId): Optional<List<ResearchProblem>> {
        val dataset = resourceService.findById(id)
        if (!dataset.isPresent) return Optional.empty()
        return Optional.of(neo4jProblemRepository
            .findResearchProblemForDataset(id)
            .map { ResearchProblem(it.resourceId!!, it.label!!) })
    }

    private fun getProblemsWithFeatured(
        classesList: List<String>,
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable,
        resultList: MutableList<DetailsPerProblem>
    ) {
        classesList.map { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> resultList.addAll(
                    neo4jProblemRepository.findPapersByProblems(
                        problemId, featured, unlisted, pageable
                    ).content
                )
                "CONTRIBUTION" -> resultList.addAll(
                    neo4jProblemRepository.findContributionsByProblems(
                        problemId, featured, unlisted, pageable
                    ).content
                )
                "COMPARISON" -> resultList.addAll(
                    neo4jProblemRepository.findComparisonsByProblems(
                        problemId,
                        featured,
                        unlisted,
                        pageable
                    ).content
                )
                "SMARTREVIEWPUBLISHED" -> resultList.addAll(
                    neo4jProblemRepository.findSmartReviewsByProblems(
                        problemId,
                        featured,
                        unlisted,
                        pageable
                    ).content
                )
                "LITERATURELISTPUBLISHED" -> resultList.addAll(
                    neo4jProblemRepository.findLiteratureListsByProblems(
                        problemId,
                        featured,
                        unlisted,
                        pageable
                    ).content
                )
                "VISUALIZATION" -> resultList.addAll(
                    neo4jProblemRepository.findVisualizationsByProblems(
                        problemId,
                        featured,
                        unlisted,
                        pageable
                    ).content
                )
                else -> {
                    resultList.addAll(
                        neo4jProblemRepository.findResearchFieldsByProblems(
                            problemId,
                            featured,
                            unlisted,
                            pageable
                        ).content
                    )
                }
            }
        }
    }

    private fun getProblemsWithoutFeatured(
        classesList: List<String>,
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable,
        resultList: MutableList<DetailsPerProblem>
    ) {
        classesList.map { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> resultList.addAll(
                    neo4jProblemRepository.findPapersByProblems(
                        problemId, unlisted, pageable
                    ).content
                )
                "CONTRIBUTION" -> resultList.addAll(
                    neo4jProblemRepository.findContributionsByProblems(
                        problemId, unlisted, pageable
                    ).content
                )
                "COMPARISON" -> resultList.addAll(
                    neo4jProblemRepository.findComparisonsByProblems(
                        problemId,
                        unlisted,
                        pageable
                    ).content
                )
                "SMARTREVIEWPUBLISHED" -> resultList.addAll(
                    neo4jProblemRepository.findSmartReviewsByProblems(
                        problemId,
                        unlisted,
                        pageable
                    ).content
                )
                "LITERATURELISTPUBLISHED" -> resultList.addAll(
                    neo4jProblemRepository.findLiteratureListsByProblems(
                        problemId,
                        unlisted,
                        pageable
                    ).content
                )
                "VISUALIZATION" -> resultList.addAll(
                    neo4jProblemRepository.findVisualizationsByProblems(
                        problemId,
                        unlisted,
                        pageable
                    ).content
                )
                else -> {
                    resultList.addAll(
                        neo4jProblemRepository.findResearchFieldsByProblems(
                            problemId,
                            unlisted,
                            pageable
                        ).content
                    )
                }
            }
        }
    }
}
