package org.orkg.contenttypes.input

interface PaperUseCases : RetrievePaperUseCase, CreatePaperUseCase, CreateContributionUseCase, UpdatePaperUseCase, PublishPaperUseCase

interface ContributionUseCases : RetrieveContributionUseCase

interface ComparisonUseCases : RetrieveComparisonUseCase, CreateComparisonUseCase, UpdateComparisonUseCase, PublishComparisonUseCase

interface VisualizationUseCases : RetrieveVisualizationUseCase, CreateVisualizationUseCase

interface TemplateUseCases : RetrieveTemplateUseCase, CreateTemplateUseCase, CreateTemplatePropertyUseCase,
    UpdateTemplateUseCase, UpdateTemplatePropertyUseCase

interface TemplateInstanceUseCases : RetrieveTemplateInstanceUseCase, UpdateTemplateInstanceUseCase

interface RosettaStoneTemplateUseCases : RetrieveRosettaStoneTemplateUseCase, CreateRosettaStoneTemplateUseCase,
    UpdateRosettaStoneTemplateUseCase, DeleteRosettaStoneTemplateUseCase

interface RosettaStoneStatementUseCases : RetrieveRosettaStoneStatementUseCase, CreateRosettaStoneStatementUseCase,
    UpdateRosettaStoneStatementUseCase, DeleteRosettaStoneStatementUseCase

interface LegacyPaperUseCases : LegacyRetrievePaperUseCase, LegacyCreatePaperUseCase

interface ResearchFieldHierarchyUseCases : RetrieveResearchFieldHierarchyUseCase

interface LiteratureListUseCases : RetrieveLiteratureListUseCase, CreateLiteratureListUseCase,
    CreateLiteratureListSectionUseCase, UpdateLiteratureListUseCase, UpdateLiteratureListSectionUseCase,
    DeleteLiteratureListSectionUseCase, PublishLiteratureListUseCase

interface SmartReviewUseCases : RetrieveSmartReviewUseCase, CreateSmartReviewUseCase, CreateSmartReviewSectionUseCase,
    UpdateSmartReviewUseCase, UpdateSmartReviewSectionUseCase, DeleteSmartReviewSectionUseCase

interface ContentTypeUseCases : RetrieveContentTypeUseCase
