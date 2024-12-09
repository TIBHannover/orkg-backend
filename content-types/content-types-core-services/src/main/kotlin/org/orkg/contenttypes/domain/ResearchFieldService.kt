package org.orkg.contenttypes.domain

import java.util.*
import org.orkg.common.ThingId
import org.orkg.community.domain.Contributor
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.contenttypes.input.RetrieveResearchFieldUseCase
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.contenttypes.output.FindResearchFieldsQuery
import org.orkg.contenttypes.output.ResearchFieldRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.PaperCountPerResearchProblem
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.input.ResourceUseCases
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ResearchFieldService(
    private val researchFieldRepository: ResearchFieldRepository,
    private val researchFieldsQuery: FindResearchFieldsQuery,
    private val contributorRepository: RetrieveContributorUseCase,
    private val comparisonRepository: ComparisonRepository,
    private val resourceService: ResourceUseCases,
) : RetrieveResearchFieldUseCase {

    override fun findById(id: ThingId): Optional<Resource> =
        resourceService.findById(id).filter { Classes.researchField in it.classes }

    override fun getResearchProblemsOfField(
        id: ThingId,
        pageable: Pageable
    ): Page<PaperCountPerResearchProblem> {
        return researchFieldRepository.getResearchProblemsOfField(id, pageable).map {
            PaperCountPerResearchProblem(
                problem = it.problem,
                papers = it.papers,
            )
        }
    }

    override fun getContributorsIncludingSubFields(id: ThingId, pageable: Pageable): Page<Contributor> {
        val contributors = researchFieldRepository.getContributorIdsFromResearchFieldAndIncludeSubfields(id, pageable)
        return PageImpl(contributorRepository.findAllByIds(contributors.content))
    }

    override fun getContributorsExcludingSubFields(id: ThingId, pageable: Pageable): Page<Contributor> {
        val contributors = researchFieldRepository.getContributorIdsExcludingSubFields(id, pageable)
        return PageImpl(contributorRepository.findAllByIds(contributors.content))
    }

    override fun findAllPapersByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> researchFieldRepository.findAllListedPapersByResearchField(id, includeSubFields, pageable)
            VisibilityFilter.UNLISTED -> researchFieldRepository.findAllPapersByResearchFieldAndVisibility(id,
                Visibility.UNLISTED, includeSubFields, pageable)
            VisibilityFilter.FEATURED -> researchFieldRepository.findAllPapersByResearchFieldAndVisibility(id,
                Visibility.FEATURED, includeSubFields, pageable)
            VisibilityFilter.NON_FEATURED -> researchFieldRepository.findAllPapersByResearchFieldAndVisibility(id,
                Visibility.DEFAULT, includeSubFields, pageable)
            VisibilityFilter.DELETED -> researchFieldRepository.findAllPapersByResearchFieldAndVisibility(id,
                Visibility.DELETED, includeSubFields, pageable)
        }

    override fun findAllResearchProblemsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> researchFieldRepository.findAllListedProblemsByResearchField(id, includeSubFields, pageable)
            VisibilityFilter.UNLISTED -> researchFieldRepository.findAllProblemsByResearchFieldAndVisibility(id,
                Visibility.UNLISTED, includeSubFields, pageable)
            VisibilityFilter.FEATURED -> researchFieldRepository.findAllProblemsByResearchFieldAndVisibility(id,
                Visibility.FEATURED, includeSubFields, pageable)
            VisibilityFilter.NON_FEATURED -> researchFieldRepository.findAllProblemsByResearchFieldAndVisibility(id,
                Visibility.DEFAULT, includeSubFields, pageable)
            VisibilityFilter.DELETED -> researchFieldRepository.findAllProblemsByResearchFieldAndVisibility(id,
                Visibility.DELETED, includeSubFields, pageable)
        }

    override fun findAllVisualizationsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> researchFieldRepository.findAllListedVisualizationsByResearchField(id, includeSubFields, pageable)
            VisibilityFilter.UNLISTED -> researchFieldRepository.findAllVisualizationsByResearchFieldAndVisibility(id,
                Visibility.UNLISTED, includeSubFields, pageable)
            VisibilityFilter.FEATURED -> researchFieldRepository.findAllVisualizationsByResearchFieldAndVisibility(id,
                Visibility.FEATURED, includeSubFields, pageable)
            VisibilityFilter.NON_FEATURED -> researchFieldRepository.findAllVisualizationsByResearchFieldAndVisibility(id,
                Visibility.DEFAULT, includeSubFields, pageable)
            VisibilityFilter.DELETED -> researchFieldRepository.findAllVisualizationsByResearchFieldAndVisibility(id,
                Visibility.DELETED, includeSubFields, pageable)
        }

    override fun findAllSmartReviewsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> researchFieldRepository.findAllListedSmartReviewsByResearchField(id, includeSubFields, pageable)
            VisibilityFilter.UNLISTED -> researchFieldRepository.findAllSmartReviewsByResearchFieldAndVisibility(id,
                Visibility.UNLISTED, includeSubFields, pageable)
            VisibilityFilter.FEATURED -> researchFieldRepository.findAllSmartReviewsByResearchFieldAndVisibility(id,
                Visibility.FEATURED, includeSubFields, pageable)
            VisibilityFilter.NON_FEATURED -> researchFieldRepository.findAllSmartReviewsByResearchFieldAndVisibility(id,
                Visibility.DEFAULT, includeSubFields, pageable)
            VisibilityFilter.DELETED -> researchFieldRepository.findAllSmartReviewsByResearchFieldAndVisibility(id,
                Visibility.DELETED, includeSubFields, pageable)
        }

    override fun findAllLiteratureListsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> researchFieldRepository.findAllListedLiteratureListsByResearchField(id, includeSubFields, pageable)
            VisibilityFilter.UNLISTED -> researchFieldRepository.findAllLiteratureListsByResearchFieldAndVisibility(id,
                Visibility.UNLISTED, includeSubFields, pageable)
            VisibilityFilter.FEATURED -> researchFieldRepository.findAllLiteratureListsByResearchFieldAndVisibility(id,
                Visibility.FEATURED, includeSubFields, pageable)
            VisibilityFilter.NON_FEATURED -> researchFieldRepository.findAllLiteratureListsByResearchFieldAndVisibility(id,
                Visibility.DEFAULT, includeSubFields, pageable)
            VisibilityFilter.DELETED -> researchFieldRepository.findAllLiteratureListsByResearchFieldAndVisibility(id,
                Visibility.DELETED, includeSubFields, pageable)
        }

    override fun findAllEntitiesBasedOnClassesByResearchField(
        id: ThingId,
        classesList: List<String>,
        visibility: VisibilityFilter,
        includeSubFields: Boolean,
        pageable: Pageable
    ): Page<Resource> {
        val pages = when (visibility) {
            VisibilityFilter.ALL_LISTED -> findAllListedEntitiesBasedOnClassesByResearchField(id, classesList, includeSubFields, pageable)
            VisibilityFilter.UNLISTED -> findAllEntitiesBasedOnClassesByResearchFieldAndVisibility(id, classesList,
                Visibility.UNLISTED, includeSubFields, pageable)
            VisibilityFilter.FEATURED -> findAllEntitiesBasedOnClassesByResearchFieldAndVisibility(id, classesList,
                Visibility.FEATURED, includeSubFields, pageable)
            VisibilityFilter.NON_FEATURED -> findAllEntitiesBasedOnClassesByResearchFieldAndVisibility(id, classesList,
                Visibility.DEFAULT, includeSubFields, pageable)
            VisibilityFilter.DELETED -> findAllEntitiesBasedOnClassesByResearchFieldAndVisibility(id, classesList,
                Visibility.DELETED, includeSubFields, pageable)
        }
        val resultList = pages.map { it.content }.flatten().sortedWith { o1, o2 ->
            o2.createdAt.compareTo(o1.createdAt)
        }
        val totalElements = pages.sumOf { it.totalElements }
        return PageImpl(resultList, pageable, totalElements)
    }

    override fun withBenchmarks(pageable: Pageable): Page<ResearchField> = researchFieldsQuery.withBenchmarks(pageable)

    private fun findAllListedEntitiesBasedOnClassesByResearchField(
        id: ThingId,
        classesList: List<String>,
        includeSubFields: Boolean,
        pageable: Pageable
    ): List<Page<Resource>> = classesList.map { classType ->
        when (classType.uppercase(Locale.getDefault())) {
            "PAPER" -> researchFieldRepository.findAllListedPapersByResearchField(id, includeSubFields, pageable)
            "COMPARISON" -> comparisonRepository.findAll(
                researchField = id,
                includeSubfields = includeSubFields,
                visibility = VisibilityFilter.ALL_LISTED,
                pageable = pageable
            )
            "VISUALIZATION" -> researchFieldRepository.findAllListedVisualizationsByResearchField(id, includeSubFields, pageable)
            "LITERATURELISTPUBLISHED" -> researchFieldRepository.findAllListedLiteratureListsByResearchField(id, includeSubFields, pageable)
            "PROBLEM" -> researchFieldRepository.findAllListedProblemsByResearchField(id, includeSubFields, pageable)
            else -> researchFieldRepository.findAllListedSmartReviewsByResearchField(id, includeSubFields, pageable)
        }
    }

    private fun findAllEntitiesBasedOnClassesByResearchFieldAndVisibility(
        id: ThingId,
        classesList: List<String>,
        visibility: Visibility,
        includeSubFields: Boolean,
        pageable: Pageable
    ): List<Page<Resource>> = classesList.map { classType ->
        when (classType.uppercase(Locale.getDefault())) {
            "PAPER" -> researchFieldRepository.findAllPapersByResearchFieldAndVisibility(id, visibility, includeSubFields, pageable)
            "COMPARISON" -> comparisonRepository.findAll(
                researchField = id,
                includeSubfields = includeSubFields,
                visibility = visibility.toVisibilityFilter(),
                pageable = pageable
            )
            "VISUALIZATION" -> researchFieldRepository.findAllVisualizationsByResearchFieldAndVisibility(id, visibility, includeSubFields, pageable)
            "LITERATURELISTPUBLISHED" -> researchFieldRepository.findAllLiteratureListsByResearchFieldAndVisibility(id, visibility, includeSubFields, pageable)
            "PROBLEM" -> researchFieldRepository.findAllProblemsByResearchFieldAndVisibility(id, visibility, includeSubFields, pageable)
            else -> researchFieldRepository.findAllSmartReviewsByResearchFieldAndVisibility(id, visibility, includeSubFields, pageable)
        }
    }
}
