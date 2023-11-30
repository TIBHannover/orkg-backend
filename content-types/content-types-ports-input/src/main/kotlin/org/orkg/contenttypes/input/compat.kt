package org.orkg.contenttypes.input

interface PaperUseCases : RetrievePaperUseCase, CreatePaperUseCase, CreateContributionUseCase, PublishPaperUseCase

interface ContributionUseCases : RetrieveContributionUseCase

interface ComparisonUseCases : RetrieveComparisonUseCase, CreateComparisonUseCase, PublishComparisonUseCase

interface VisualizationUseCases : RetrieveVisualizationUseCase

interface TemplateUseCases : RetrieveTemplateUseCase

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
