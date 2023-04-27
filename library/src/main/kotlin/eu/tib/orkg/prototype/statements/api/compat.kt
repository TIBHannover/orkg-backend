package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.application.port.`in`.MarkAsUnlistedService
import eu.tib.orkg.prototype.statements.application.port.`in`.MarkAsVerifiedUseCase
import eu.tib.orkg.prototype.statements.application.port.`in`.MarkFeaturedService
import eu.tib.orkg.prototype.statements.application.port.out.GetComparisonFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.GetContributionFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.GetContributorsQuery
import eu.tib.orkg.prototype.statements.application.port.out.GetPaperFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.GetResourceFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.GetSmartReviewFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.GetVisualizationFlagQuery
import eu.tib.orkg.prototype.statements.application.port.out.LoadComparisonPort
import eu.tib.orkg.prototype.statements.application.port.out.LoadContributionPort
import eu.tib.orkg.prototype.statements.application.port.out.LoadPaperPort
import eu.tib.orkg.prototype.statements.application.port.out.LoadSmartReviewPort
import eu.tib.orkg.prototype.statements.application.port.out.LoadVisualizationPort
import eu.tib.orkg.prototype.statements.domain.model.URIService

interface ClassUseCases : CreateClassUseCase, RetrieveClassUseCase, UpdateClassUseCase, DeleteClassUseCase,
    URIService<ClassRepresentation>

interface LiteralUseCases : CreateLiteralUseCase, RetrieveLiteralUseCase, UpdateLiteralUseCase, DeleteLiteralUseCase

interface PredicateUseCases : CreatePredicateUseCase, DeletePredicateUseCase, UpdatePredicateUseCase,
    RetrievePredicateUseCase

interface ResourceUseCases : CreateResourceUseCase, RetrieveResourceUseCase, UpdateResourceUseCase,
    DeleteResourceUseCase, OtherResourceUseCases

interface StatementUseCases : CreateStatementUseCase, RetrieveStatementUseCase, UpdateStatementUseCase,
    DeleteStatementUseCase

// FIXME: we need to refactor those as well
interface OtherResourceUseCases :
    MarkAsVerifiedUseCase,
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
    GetSmartReviewFlagQuery,
    GetContributorsQuery
