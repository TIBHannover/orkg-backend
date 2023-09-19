package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.community.domain.model.ResearchField
import eu.tib.orkg.prototype.community.domain.model.Contributor
import eu.tib.orkg.prototype.community.spi.ContributorRepository
import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchFieldsQuery
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldUseCase
import eu.tib.orkg.prototype.statements.api.RetrieveResearchFieldUseCase.*
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ResearchFieldRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val ResearchField = ThingId("ResearchField")

@Service
@Transactional
class ResearchFieldService(
    private val researchFieldRepository: ResearchFieldRepository,
    private val researchFieldsQuery: FindResearchFieldsQuery,
    private val contributorRepository: ContributorRepository,
    private val resourceService: ResourceUseCases,
) : RetrieveResearchFieldUseCase {

    override fun findById(id: ThingId): Optional<Resource> =
        Optional.ofNullable(resourceService.findByIdAndClasses(id, setOf(ResearchField)))

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
            VisibilityFilter.UNLISTED -> researchFieldRepository.findAllPapersByResearchFieldAndVisibility(id, Visibility.UNLISTED, includeSubFields, pageable)
            VisibilityFilter.FEATURED -> researchFieldRepository.findAllPapersByResearchFieldAndVisibility(id, Visibility.FEATURED, includeSubFields, pageable)
            VisibilityFilter.NON_FEATURED -> researchFieldRepository.findAllPapersByResearchFieldAndVisibility(id, Visibility.DEFAULT, includeSubFields, pageable)
            VisibilityFilter.DELETED -> researchFieldRepository.findAllPapersByResearchFieldAndVisibility(id, Visibility.DELETED, includeSubFields, pageable)
        }

    override fun findAllComparisonsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> researchFieldRepository.findAllListedComparisonsByResearchField(id, includeSubFields, pageable)
            VisibilityFilter.UNLISTED -> researchFieldRepository.findAllComparisonsByResearchFieldAndVisibility(id, Visibility.UNLISTED, includeSubFields, pageable)
            VisibilityFilter.FEATURED -> researchFieldRepository.findAllComparisonsByResearchFieldAndVisibility(id, Visibility.FEATURED, includeSubFields, pageable)
            VisibilityFilter.NON_FEATURED -> researchFieldRepository.findAllComparisonsByResearchFieldAndVisibility(id, Visibility.DEFAULT, includeSubFields, pageable)
            VisibilityFilter.DELETED -> researchFieldRepository.findAllComparisonsByResearchFieldAndVisibility(id, Visibility.DELETED, includeSubFields, pageable)
        }

    override fun findAllResearchProblemsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> researchFieldRepository.findAllListedProblemsByResearchField(id, includeSubFields, pageable)
            VisibilityFilter.UNLISTED -> researchFieldRepository.findAllProblemsByResearchFieldAndVisibility(id, Visibility.UNLISTED, includeSubFields, pageable)
            VisibilityFilter.FEATURED -> researchFieldRepository.findAllProblemsByResearchFieldAndVisibility(id, Visibility.FEATURED, includeSubFields, pageable)
            VisibilityFilter.NON_FEATURED -> researchFieldRepository.findAllProblemsByResearchFieldAndVisibility(id, Visibility.DEFAULT, includeSubFields, pageable)
            VisibilityFilter.DELETED -> researchFieldRepository.findAllProblemsByResearchFieldAndVisibility(id, Visibility.DELETED, includeSubFields, pageable)
        }

    override fun findAllVisualizationsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> researchFieldRepository.findAllListedVisualizationsByResearchField(id, includeSubFields, pageable)
            VisibilityFilter.UNLISTED -> researchFieldRepository.findAllVisualizationsByResearchFieldAndVisibility(id, Visibility.UNLISTED, includeSubFields, pageable)
            VisibilityFilter.FEATURED -> researchFieldRepository.findAllVisualizationsByResearchFieldAndVisibility(id, Visibility.FEATURED, includeSubFields, pageable)
            VisibilityFilter.NON_FEATURED -> researchFieldRepository.findAllVisualizationsByResearchFieldAndVisibility(id, Visibility.DEFAULT, includeSubFields, pageable)
            VisibilityFilter.DELETED -> researchFieldRepository.findAllVisualizationsByResearchFieldAndVisibility(id, Visibility.DELETED, includeSubFields, pageable)
        }

    override fun findAllSmartReviewsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> researchFieldRepository.findAllListedSmartReviewsByResearchField(id, includeSubFields, pageable)
            VisibilityFilter.UNLISTED -> researchFieldRepository.findAllSmartReviewsByResearchFieldAndVisibility(id, Visibility.UNLISTED, includeSubFields, pageable)
            VisibilityFilter.FEATURED -> researchFieldRepository.findAllSmartReviewsByResearchFieldAndVisibility(id, Visibility.FEATURED, includeSubFields, pageable)
            VisibilityFilter.NON_FEATURED -> researchFieldRepository.findAllSmartReviewsByResearchFieldAndVisibility(id, Visibility.DEFAULT, includeSubFields, pageable)
            VisibilityFilter.DELETED -> researchFieldRepository.findAllSmartReviewsByResearchFieldAndVisibility(id, Visibility.DELETED, includeSubFields, pageable)
        }

    override fun findAllLiteratureListsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> researchFieldRepository.findAllListedLiteratureListsByResearchField(id, includeSubFields, pageable)
            VisibilityFilter.UNLISTED -> researchFieldRepository.findAllLiteratureListsByResearchFieldAndVisibility(id, Visibility.UNLISTED, includeSubFields, pageable)
            VisibilityFilter.FEATURED -> researchFieldRepository.findAllLiteratureListsByResearchFieldAndVisibility(id, Visibility.FEATURED, includeSubFields, pageable)
            VisibilityFilter.NON_FEATURED -> researchFieldRepository.findAllLiteratureListsByResearchFieldAndVisibility(id, Visibility.DEFAULT, includeSubFields, pageable)
            VisibilityFilter.DELETED -> researchFieldRepository.findAllLiteratureListsByResearchFieldAndVisibility(id, Visibility.DELETED, includeSubFields, pageable)
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
            VisibilityFilter.UNLISTED -> findAllEntitiesBasedOnClassesByResearchFieldAndVisibility(id, classesList, Visibility.UNLISTED, includeSubFields, pageable)
            VisibilityFilter.FEATURED -> findAllEntitiesBasedOnClassesByResearchFieldAndVisibility(id, classesList, Visibility.FEATURED, includeSubFields, pageable)
            VisibilityFilter.NON_FEATURED -> findAllEntitiesBasedOnClassesByResearchFieldAndVisibility(id, classesList, Visibility.DEFAULT, includeSubFields, pageable)
            VisibilityFilter.DELETED -> findAllEntitiesBasedOnClassesByResearchFieldAndVisibility(id, classesList, Visibility.DELETED, includeSubFields, pageable)
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
            "COMPARISON" -> researchFieldRepository.findAllListedComparisonsByResearchField(id, includeSubFields, pageable)
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
            "COMPARISON" -> researchFieldRepository.findAllComparisonsByResearchFieldAndVisibility(id, visibility, includeSubFields, pageable)
            "VISUALIZATION" -> researchFieldRepository.findAllVisualizationsByResearchFieldAndVisibility(id, visibility, includeSubFields, pageable)
            "LITERATURELISTPUBLISHED" -> researchFieldRepository.findAllLiteratureListsByResearchFieldAndVisibility(id, visibility, includeSubFields, pageable)
            "PROBLEM" -> researchFieldRepository.findAllProblemsByResearchFieldAndVisibility(id, visibility, includeSubFields, pageable)
            else -> researchFieldRepository.findAllSmartReviewsByResearchFieldAndVisibility(id, visibility, includeSubFields, pageable)
        }
    }
}
