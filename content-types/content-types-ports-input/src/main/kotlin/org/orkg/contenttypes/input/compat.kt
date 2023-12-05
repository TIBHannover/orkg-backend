package org.orkg.contenttypes.input

import org.orkg.contenttypes.input.CreateVisualizationUseCase

interface PaperUseCases : RetrievePaperUseCase, CreatePaperUseCase, CreateContributionUseCase, PublishPaperUseCase

interface ContributionUseCases : RetrieveContributionUseCase

interface ComparisonUseCases : RetrieveComparisonUseCase, CreateComparisonUseCase, PublishComparisonUseCase

interface VisualizationUseCases : RetrieveVisualizationUseCase, CreateVisualizationUseCase

interface TemplateUseCases : RetrieveTemplateUseCase, CreateTemplateUseCase, CreateTemplatePropertyUseCase

interface ContentTypeResourcesUseCase :
    LoadPaperPort,
    GetPaperFlagQuery,
    LoadComparisonPort,
    LoadContributionPort,
    LoadVisualizationPort,
    LoadSmartReviewPort,
    GetContributionFlagQuery,
    GetComparisonFlagQuery,
    GetVisualizationFlagQuery,
    GetSmartReviewFlagQuery

interface LegacyPaperUseCases : LegacyRetrievePaperUseCase, LegacyCreatePaperUseCase

interface ResearchFieldHierarchyUseCases : RetrieveResearchFieldHierarchyUseCase
