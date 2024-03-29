package org.orkg.contenttypes.input

interface PaperUseCases : RetrievePaperUseCase, CreatePaperUseCase, CreateContributionUseCase, UpdatePaperUseCase, PublishPaperUseCase

interface ContributionUseCases : RetrieveContributionUseCase

interface ComparisonUseCases : RetrieveComparisonUseCase, CreateComparisonUseCase, PublishComparisonUseCase

interface VisualizationUseCases : RetrieveVisualizationUseCase, CreateVisualizationUseCase

interface TemplateUseCases : RetrieveTemplateUseCase, CreateTemplateUseCase, CreateTemplatePropertyUseCase,
    UpdateTemplateUseCase, UpdateTemplatePropertyUseCase

interface TemplateInstanceUseCases : RetrieveTemplateInstanceUseCase, UpdateTemplateInstanceUseCase

interface RosettaTemplateUseCases : RetrieveRosettaTemplateUseCase

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

interface LiteratureListUseCases : RetrieveLiteratureListUseCase

interface SmartReviewUseCases : RetrieveSmartReviewUseCase
