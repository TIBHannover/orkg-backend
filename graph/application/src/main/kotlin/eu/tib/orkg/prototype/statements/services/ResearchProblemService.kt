package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchProblemQuery
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.RetrieveResearchProblemUseCase
import eu.tib.orkg.prototype.statements.api.RetrieveResearchProblemUseCase.FieldWithFreq
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ResearchProblemRepository
import eu.tib.orkg.prototype.statements.spi.ResearchProblemRepository.ContributorPerProblem
import java.time.format.DateTimeFormatter
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
    override fun findById(id: ThingId): Optional<Resource> =
        Optional.ofNullable(resourceService.findByIdAndClasses(id, setOf(ProblemClass)))

    override fun findFieldsPerProblem(problemId: ThingId): List<FieldWithFreq> {
        return researchProblemRepository.findResearchFieldsPerProblem(problemId).map {
            FieldWithFreq(it.field, it.freq)
        }
    }

    override fun findAllEntitiesBasedOnClassByProblem(
        problemId: ThingId,
        classes: List<String>,
        visibilityFilter: VisibilityFilter,
        pageable: Pageable
    ): Page<RetrieveResearchProblemUseCase.DetailsPerProblem> {
        val resultList = mutableListOf<Resource>()
        val totals = mutableListOf<Long>()
        if (classes.isNotEmpty()) {
            when (visibilityFilter) {
                VisibilityFilter.ALL_LISTED -> findAllListedEntitiesBasedOnClassByProblem(classes, problemId, pageable, resultList, totals)
                VisibilityFilter.UNLISTED -> findAllEntitiesBasedOnClassByProblemAndVisibility(classes, problemId, Visibility.UNLISTED, pageable, resultList, totals)
                VisibilityFilter.FEATURED -> findAllEntitiesBasedOnClassByProblemAndVisibility(classes, problemId, Visibility.FEATURED, pageable, resultList, totals)
                VisibilityFilter.NON_FEATURED -> findAllEntitiesBasedOnClassByProblemAndVisibility(classes, problemId, Visibility.DEFAULT, pageable, resultList, totals)
                VisibilityFilter.DELETED -> findAllEntitiesBasedOnClassByProblemAndVisibility(classes, problemId, Visibility.DELETED, pageable, resultList, totals)
            }
            resultList.sortBy(Resource::createdAt)
        } else {
            return Page.empty(pageable)
        }
        return PageImpl(resultList.take(pageable.pageSize), pageable, totals.sum()).map {
            RetrieveResearchProblemUseCase.DetailsPerProblem(
                id = it.id,
                label = it.label,
                createdAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(it.createdAt),
                featured = it.visibility == Visibility.FEATURED,
                unlisted = it.visibility == Visibility.UNLISTED,
                classes = it.classes.map { `class` -> `class`.value },
                createdBy = it.createdBy.value.toString()
            )
        }
    }

    override fun findTopResearchProblems(): List<Resource> =
        findTopResearchProblemsGoingBack(
            listOf(1, 2, 3, 6), emptyList()
        ).toList()

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

    override fun forDataset(id: ThingId, pageable: Pageable): Optional<Page<ResearchProblem>> {
        val dataset = resourceService.findById(id)
        if (!dataset.isPresent) return Optional.empty()
        return Optional.of(researchProblemQueries.findResearchProblemForDataset(id, pageable))
    }

    override fun getFeaturedProblemFlag(id: ThingId): Boolean =
        researchProblemRepository.findById(id)
            .map { it.visibility == Visibility.FEATURED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun getUnlistedProblemFlag(id: ThingId): Boolean =
        researchProblemRepository.findById(id)
            .map { it.visibility == Visibility.UNLISTED || it.visibility == Visibility.DELETED }
            .orElseThrow { ResourceNotFound.withId(id) }

    override fun loadFeaturedProblems(pageable: Pageable): Page<Resource> =
        researchProblemRepository.findAllProblemsByVisibility(Visibility.FEATURED, pageable)

    override fun loadNonFeaturedProblems(pageable: Pageable): Page<Resource> =
        researchProblemRepository.findAllProblemsByVisibility(Visibility.DEFAULT, pageable)

    override fun loadUnlistedProblems(pageable: Pageable): Page<Resource> =
        researchProblemRepository.findAllProblemsByVisibility(Visibility.UNLISTED, pageable)

    override fun loadListedProblems(pageable: Pageable): Page<Resource> =
        researchProblemRepository.findAllListedProblems(pageable)

    private fun findAllListedEntitiesBasedOnClassByProblem(
        classesList: List<String>,
        problemId: ThingId,
        pageable: Pageable,
        resultList: MutableList<Resource>,
        totals: MutableList<Long>,
    ) {
        classesList.map { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> {
                    val result = researchProblemRepository.findAllListedPapersByProblem(problemId, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "CONTRIBUTION" -> {
                    val result = researchProblemRepository.findAllListedContributionsByProblem(problemId, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "COMPARISON" -> {
                    val result = researchProblemRepository.findAllListedComparisonsByProblem(problemId, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "SMARTREVIEWPUBLISHED" -> {
                    val result = researchProblemRepository.findAllListedSmartReviewsByProblem(problemId, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "LITERATURELISTPUBLISHED" -> {
                    val result = researchProblemRepository.findAllListedLiteratureListsByProblem(problemId, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "VISUALIZATION" -> {
                    val result = researchProblemRepository.findAllListedVisualizationsByProblem(problemId, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                else -> {
                    val result = researchProblemRepository.findAllListedResearchFieldsByProblem(problemId, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
            }
        }
    }

    private fun findAllEntitiesBasedOnClassByProblemAndVisibility(
        classesList: List<String>,
        problemId: ThingId,
        visibility: Visibility,
        pageable: Pageable,
        resultList: MutableList<Resource>,
        totals: MutableList<Long>,
    ) {
        classesList.forEach { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> {
                    val result = researchProblemRepository.findAllPapersByProblemAndVisibility(problemId, visibility, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "CONTRIBUTION" -> {
                    val result = researchProblemRepository.findAllContributionsByProblemAndVisibility(problemId, visibility, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "COMPARISON" -> {
                    val result = researchProblemRepository.findAllComparisonsByProblemAndVisibility(problemId, visibility, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "SMARTREVIEWPUBLISHED" -> {
                    val result = researchProblemRepository.findAllSmartReviewsByProblemAndVisibility(problemId, visibility, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "LITERATURELISTPUBLISHED" -> {
                    val result = researchProblemRepository.findAllLiteratureListsByProblemAndVisibility(problemId, visibility, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "VISUALIZATION" -> {
                    val result = researchProblemRepository.findAllVisualizationsByProblemAndVisibility(problemId, visibility, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                else -> {
                    val result = researchProblemRepository.findAllResearchFieldsByProblemAndVisibility(problemId, visibility, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
            }
        }
    }
}
