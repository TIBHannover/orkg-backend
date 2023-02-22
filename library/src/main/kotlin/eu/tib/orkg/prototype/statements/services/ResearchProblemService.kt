package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchProblemQuery
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.api.IterableResourcesGenerator
import eu.tib.orkg.prototype.statements.api.ResourceGenerator
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.RetrieveResearchProblemUseCase
import eu.tib.orkg.prototype.statements.api.RetrieveResearchProblemUseCase.FieldCount
import eu.tib.orkg.prototype.statements.api.RetrieveResearchProblemUseCase.PaperCountPerAuthor
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResearchProblemRepository
import eu.tib.orkg.prototype.statements.spi.ResearchProblemRepository.ContributorPerProblem
import eu.tib.orkg.prototype.statements.spi.ResearchProblemRepository.DetailsPerProblem
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val ProblemClass = ThingId("Problem")

@Service
@Transactional
class ResearchProblemService(
    private val researchProblemRepository: ResearchProblemRepository,
    private val resourceService: ResourceUseCases,
    private val researchProblemQueries: FindResearchProblemQuery,
) : RetrieveResearchProblemUseCase {
    override fun findById(id: ThingId): Optional<ResourceRepresentation> =
        Optional.ofNullable(resourceService.findByIdAndClasses(id, setOf(ProblemClass)))

    override fun findFieldsPerProblem(problemId: ThingId): List<FieldCount> {
        return researchProblemRepository.findResearchFieldsPerProblem(problemId).map {
            FieldCount(
                field = resourceService.map(ResourceGenerator { it.field }), freq = it.freq
            )
        }
    }

    override fun findFieldsPerProblemAndClasses(
        problemId: ThingId,
        featured: Boolean?,
        unlisted: Boolean,
        classes: List<String>,
        pageable: Pageable
    ): Page<DetailsPerProblem> {
        val resultList = mutableListOf<DetailsPerProblem>()
        val totals = mutableListOf<Long>()
        if (classes.isNotEmpty()) {
            when (featured) {
                null -> getProblemsWithoutFeatured(classes, problemId, unlisted, pageable, resultList, totals)
                else -> getProblemsWithFeatured(classes, problemId, featured, unlisted, pageable, resultList, totals)
            }
            resultList.sortBy(DetailsPerProblem::createdAt)
        } else {
            return Page.empty()
        }
        return PageImpl(resultList.take(pageable.pageSize), pageable, totals.sum())
    }

    override fun findTopResearchProblems(): List<ResourceRepresentation> =
        resourceService.map(IterableResourcesGenerator {
            findTopResearchProblemsGoingBack(
                listOf(1, 2, 3, 6), emptyList()
            )
        }).toList()

    /*
    Iterate over the list of months, and if no problems are found go back a bit more in time
    and if none found take all time results
     */
    private fun findTopResearchProblemsGoingBack(
        listOfMonths: List<Int>,
        result: List<Resource>
    ): Iterable<Resource> {
        val month = listOfMonths.firstOrNull()
        val problems = if (month == null)
            researchProblemRepository.findTopResearchProblemsAllTime()
        else
            researchProblemRepository.findTopResearchProblemsGoingBack(month)
        val newResult = result.plus(problems).distinct()
        return if (newResult.count() >= 5)
            newResult.take(5)
        else
            findTopResearchProblemsGoingBack(listOfMonths.drop(1), newResult)
    }

    override fun findContributorsPerProblem(problemId: ThingId, pageable: Pageable): List<ContributorPerProblem> {
        return researchProblemRepository
            .findContributorsLeaderboardPerProblem(problemId, pageable)
            .content
    }

    override fun findAuthorsPerProblem(problemId: ThingId, pageable: Pageable): List<PaperCountPerAuthor> {
        return researchProblemRepository.findAuthorsLeaderboardPerProblem(problemId, pageable).content.map {
                if (it.isLiteral) PaperCountPerAuthor(
                    author = it.author,
                    papers = it.papers,
                )
                else PaperCountPerAuthor(
                    // It is important that this is a ResourceRepresentation! Otherwise, it will break clients.
                    // This is not ideal, but would need custom serialization code.
                    author = resourceService.map(ResourceGenerator { it.toAuthorResource }),
                    papers = it.papers,
                )
            }
    }

    override fun forDataset(id: ThingId): Optional<List<ResearchProblem>> {
        val dataset = resourceService.findById(id)
        if (!dataset.isPresent) return Optional.empty()
        return Optional.of(researchProblemQueries.findResearchProblemForDataset(id))
    }

    override fun getFeaturedProblemFlag(id: ThingId): Boolean {
        val result = researchProblemRepository.findById(id)
        return result.orElseThrow { ResourceNotFound.withId(id) }.featured ?: false
    }

    override fun getUnlistedProblemFlag(id: ThingId): Boolean {
        val result = researchProblemRepository.findById(id)
        return result.orElseThrow { ResourceNotFound.withId(id) }.unlisted ?: false
    }

    override fun loadFeaturedProblems(pageable: Pageable): Page<Resource> =
        researchProblemRepository.findAllFeaturedProblems(pageable)

    override fun loadNonFeaturedProblems(pageable: Pageable): Page<Resource> =
        researchProblemRepository.findAllNonFeaturedProblems(pageable)

    override fun loadUnlistedProblems(pageable: Pageable): Page<Resource> =
        researchProblemRepository.findAllUnlistedProblems(pageable)

    override fun loadListedProblems(pageable: Pageable): Page<Resource> =
        researchProblemRepository.findAllListedProblems(pageable)

    private fun getProblemsWithFeatured(
        classesList: List<String>,
        problemId: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable,
        resultList: MutableList<DetailsPerProblem>,
        totals: MutableList<Long>,
    ) {
        classesList.forEach { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> {
                    val result = researchProblemRepository.findPapersByProblems(problemId, featured, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "CONTRIBUTION" -> {
                    val result =
                        researchProblemRepository.findContributionsByProblems(problemId, featured, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "COMPARISON" -> {
                    val result =
                        researchProblemRepository.findComparisonsByProblems(problemId, featured, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "SMARTREVIEWPUBLISHED" -> {
                    val result =
                        researchProblemRepository.findSmartReviewsByProblems(problemId, featured, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "LITERATURELISTPUBLISHED" -> {
                    val result =
                        researchProblemRepository.findLiteratureListsByProblems(problemId, featured, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "VISUALIZATION" -> {
                    val result =
                        researchProblemRepository.findVisualizationsByProblems(problemId, featured, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                else -> {
                    val result =
                        researchProblemRepository.findResearchFieldsByProblems(problemId, featured, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
            }
        }
    }

    private fun getProblemsWithoutFeatured(
        classesList: List<String>,
        problemId: ThingId,
        unlisted: Boolean,
        pageable: Pageable,
        resultList: MutableList<DetailsPerProblem>,
        totals: MutableList<Long>,
    ) {
        classesList.forEach { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> {
                    val result = researchProblemRepository.findPapersByProblems(problemId, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "CONTRIBUTION" -> {
                    val result = researchProblemRepository.findContributionsByProblems(problemId, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "COMPARISON" -> {
                    val result = researchProblemRepository.findComparisonsByProblems(problemId, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "SMARTREVIEWPUBLISHED" -> {
                    val result = researchProblemRepository.findSmartReviewsByProblems(problemId, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "LITERATURELISTPUBLISHED" -> {
                    val result = researchProblemRepository.findLiteratureListsByProblems(problemId, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "VISUALIZATION" -> {
                    val result = researchProblemRepository.findVisualizationsByProblems(problemId, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                else -> {
                    val result = researchProblemRepository.findResearchFieldsByProblems(problemId, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
            }
        }
    }
}
