package org.orkg.contenttypes.input

interface PaperUseCases : RetrievePaperUseCase, CreatePaperUseCase, CreateContributionUseCase, UpdatePaperUseCase, PublishPaperUseCase

interface ContributionUseCases : RetrieveContributionUseCase

interface ComparisonUseCases : RetrieveComparisonUseCase, CreateComparisonUseCase, UpdateComparisonUseCase, PublishComparisonUseCase

interface VisualizationUseCases : RetrieveVisualizationUseCase, CreateVisualizationUseCase

interface TemplateUseCases : RetrieveTemplateUseCase, CreateTemplateUseCase, CreateTemplatePropertyUseCase,
    UpdateTemplateUseCase, UpdateTemplatePropertyUseCase

interface TemplateInstanceUseCases : RetrieveTemplateInstanceUseCase, UpdateTemplateInstanceUseCase

interface RosettaTemplateUseCases : RetrieveRosettaTemplateUseCase

interface LegacyPaperUseCases : LegacyRetrievePaperUseCase, LegacyCreatePaperUseCase

interface ResearchFieldHierarchyUseCases : RetrieveResearchFieldHierarchyUseCase

interface LiteratureListUseCases : RetrieveLiteratureListUseCase

interface SmartReviewUseCases : RetrieveSmartReviewUseCase

interface ContentTypeUseCases : RetrieveContentTypeUseCase
