package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.contenttypes.input.ResearchProblemUseCases
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.contenttypes.output.FindResearchProblemQuery
import org.orkg.contenttypes.output.ResearchProblemRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ContributorPerProblem
import org.orkg.graph.domain.DetailsPerProblem
import org.orkg.graph.domain.FieldWithFreq
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ResourceUseCases
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Optional

@Service
@TransactionalOnNeo4j
class ResearchProblemService(
    private val researchProblemRepository: ResearchProblemRepository,
    private val resourceService: ResourceUseCases,
    private val researchProblemQueries: FindResearchProblemQuery,
    private val comparisonRepository: ComparisonRepository,
) : ResearchProblemUseCases {
    override fun findById(id: ThingId): Optional<Resource> =
        resourceService.findById(id).filter { Classes.problem in it.classes }

    override fun findAllResearchFields(problemId: ThingId): List<FieldWithFreq> = researchProblemRepository.findAllResearchFieldsWithPaperCountByProblemId(problemId).map {
        FieldWithFreq(it.field, it.freq)
    }

    override fun findAllEntitiesBasedOnClassByProblem(
        problemId: ThingId,
        classes: List<String>,
        visibilityFilter: VisibilityFilter,
        pageable: Pageable,
    ): Page<DetailsPerProblem> {
        val resultList = mutableListOf<Resource>()
        val totals = mutableListOf<Long>()
        if (classes.isNotEmpty()) {
            when (visibilityFilter) {
                VisibilityFilter.ALL_LISTED -> findAllListedEntitiesBasedOnClassByProblem(classes, problemId, pageable, resultList, totals)
                VisibilityFilter.UNLISTED -> findAllEntitiesBasedOnClassByProblemAndVisibility(
                    classes,
                    problemId,
                    Visibility.UNLISTED,
                    pageable,
                    resultList,
                    totals
                )
                VisibilityFilter.FEATURED -> findAllEntitiesBasedOnClassByProblemAndVisibility(
                    classes,
                    problemId,
                    Visibility.FEATURED,
                    pageable,
                    resultList,
                    totals
                )
                VisibilityFilter.NON_FEATURED -> findAllEntitiesBasedOnClassByProblemAndVisibility(
                    classes,
                    problemId,
                    Visibility.DEFAULT,
                    pageable,
                    resultList,
                    totals
                )
                VisibilityFilter.DELETED -> findAllEntitiesBasedOnClassByProblemAndVisibility(
                    classes,
                    problemId,
                    Visibility.DELETED,
                    pageable,
                    resultList,
                    totals
                )
            }
            resultList.sortBy(Resource::createdAt)
        } else {
            return Page.empty(pageable)
        }
        return PageImpl(resultList.take(pageable.pageSize), pageable, totals.sum()).map {
            DetailsPerProblem(
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
            listOf(1, 2, 3, 6),
            emptyList()
        ).toList()

    /*
    Iterate over the list of months, and if no problems are found go back a bit more in time
    and if none found take all time results
     */
    private fun findTopResearchProblemsGoingBack(
        listOfMonths: List<Int>,
        result: List<Resource>,
    ): Iterable<Resource> {
        val month = listOfMonths.firstOrNull()
        val problems = if (month == null) {
            researchProblemRepository.findTopResearchProblemsAllTime()
        } else {
            researchProblemRepository.findTopResearchProblemsGoingBack(month)
        }
        val newResult = result.plus(problems).distinct()
        return if (newResult.count() >= 5) {
            newResult.take(5)
        } else {
            findTopResearchProblemsGoingBack(listOfMonths.drop(1), newResult)
        }
    }

    override fun findAllContributorsPerProblem(problemId: ThingId, pageable: Pageable): List<ContributorPerProblem> = researchProblemRepository
        .findAllContributorsPerProblem(problemId, pageable)
        .content

    override fun findAllByDatasetId(id: ThingId, pageable: Pageable): Optional<Page<ResearchProblem>> {
        val dataset = resourceService.findById(id)
        if (!dataset.isPresent) return Optional.empty()
        return Optional.of(researchProblemQueries.findAllByDatasetId(id, pageable))
    }

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
                    val result = comparisonRepository.findAll(
                        pageable,
                        researchProblem = problemId,
                        visibility = VisibilityFilter.ALL_LISTED,
                        published = true
                    )
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
                    val result = comparisonRepository.findAll(
                        pageable = pageable,
                        researchProblem = problemId,
                        visibility = visibility.toVisibilityFilter(),
                        published = true
                    )
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
