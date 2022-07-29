package eu.tib.orkg.prototype.contenttypes.api

import eu.tib.orkg.prototype.statements.application.port.`in`.MarkAsUnlistedService
import eu.tib.orkg.prototype.statements.application.port.`in`.MarkAsVerifiedUseCase
import eu.tib.orkg.prototype.statements.application.port.`in`.MarkFeaturedService
import eu.tib.orkg.prototype.statements.application.port.out.GetComparisonFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.GetContributionFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.GetPaperFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.GetResourceFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.GetSmartReviewFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.GetVisualizationFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.LoadComparisonPort
import eu.tib.orkg.prototype.statements.application.port.out.LoadContributionPort
import eu.tib.orkg.prototype.statements.application.port.out.LoadPaperPort
import eu.tib.orkg.prototype.statements.application.port.out.LoadResourcePort
import eu.tib.orkg.prototype.statements.application.port.out.LoadSmartReviewPort
import eu.tib.orkg.prototype.statements.application.port.out.LoadVisualizationPort
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ContentTypeUseCase :
    MarkAsVerifiedUseCase,
    LoadResourcePort,
    LoadPaperPort,
    GetPaperFlagQuery,
    MarkFeaturedService,
    MarkAsUnlistedService,
    GetResourceFlagQuery,
    LoadComparisonPort,
    LoadContributionPort,
    LoadVisualizationPort,
    LoadSmartReviewPort,
    GetContributionFlagQuery,
    GetComparisonFlagQuery,
    GetVisualizationFlagQuery,
    GetSmartReviewFlagQuery {
    // Legacy methods: ResearchProblem
    fun getFeaturedProblemFlag(id: ResourceId): Boolean
    fun getUnlistedProblemFlag(id: ResourceId): Boolean
    fun loadFeaturedProblems(pageable: Pageable): Page<Resource>
    fun loadNonFeaturedProblems(pageable: Pageable): Page<Resource>
    fun loadUnlistedProblems(pageable: Pageable): Page<Resource>
    fun loadListedProblems(pageable: Pageable): Page<Resource>
}
