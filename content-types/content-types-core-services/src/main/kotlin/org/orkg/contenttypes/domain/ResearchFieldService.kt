package org.orkg.contenttypes.domain

import org.orkg.common.ThingId
import org.orkg.community.domain.Contributor
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.contenttypes.input.ResearchFieldUseCases
import org.orkg.contenttypes.output.FindResearchFieldsQuery
import org.orkg.contenttypes.output.ResearchFieldRepository
import org.orkg.graph.domain.PaperCountPerResearchProblem
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.spring.data.annotations.TransactionalOnNeo4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@TransactionalOnNeo4j
class ResearchFieldService(
    private val researchFieldRepository: ResearchFieldRepository,
    private val researchFieldsQuery: FindResearchFieldsQuery,
    private val contributorRepository: RetrieveContributorUseCase,
) : ResearchFieldUseCases {
    override fun findAllPaperCountsPerResearchProblem(
        id: ThingId,
        pageable: Pageable,
    ): Page<PaperCountPerResearchProblem> = researchFieldRepository.findAllPaperCountsPerResearchProblem(id, pageable).map {
        PaperCountPerResearchProblem(
            problem = it.problem,
            papers = it.papers,
        )
    }

    override fun findAllContributorsIncludingSubFields(id: ThingId, pageable: Pageable): Page<Contributor> {
        val contributors = researchFieldRepository.findAllContributorIdsIncludingSubFields(id, pageable)
        return PageImpl(contributorRepository.findAllById(contributors.content), pageable, contributors.totalElements)
    }

    override fun findAllContributorsExcludingSubFields(id: ThingId, pageable: Pageable): Page<Contributor> {
        val contributors = researchFieldRepository.findAllContributorIdsExcludingSubFields(id, pageable)
        return PageImpl(contributorRepository.findAllById(contributors.content), pageable, contributors.totalElements)
    }

    override fun findAllResearchProblemsByResearchField(
        id: ThingId,
        visibility: VisibilityFilter,
        includeSubFields: Boolean,
        pageable: Pageable,
    ): Page<Resource> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> researchFieldRepository.findAllListedProblemsByResearchField(id, includeSubFields, pageable)
            VisibilityFilter.UNLISTED -> researchFieldRepository.findAllProblemsByResearchFieldAndVisibility(
                id,
                Visibility.UNLISTED,
                includeSubFields,
                pageable
            )
            VisibilityFilter.FEATURED -> researchFieldRepository.findAllProblemsByResearchFieldAndVisibility(
                id,
                Visibility.FEATURED,
                includeSubFields,
                pageable
            )
            VisibilityFilter.NON_FEATURED -> researchFieldRepository.findAllProblemsByResearchFieldAndVisibility(
                id,
                Visibility.DEFAULT,
                includeSubFields,
                pageable
            )
            VisibilityFilter.DELETED -> researchFieldRepository.findAllProblemsByResearchFieldAndVisibility(
                id,
                Visibility.DELETED,
                includeSubFields,
                pageable
            )
        }

    override fun findAllWithBenchmarks(pageable: Pageable): Page<ResearchField> =
        researchFieldsQuery.findAllWithBenchmarks(pageable)
}
