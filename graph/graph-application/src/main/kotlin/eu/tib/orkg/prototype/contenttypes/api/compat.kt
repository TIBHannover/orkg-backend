package eu.tib.orkg.prototype.contenttypes.api

interface PaperUseCases : RetrievePaperUseCase, CreatePaperUseCase, CreateContributionUseCase, PublishPaperUseCase

interface ContributionUseCases : RetrieveContributionUseCase

interface ComparisonUseCases : RetrieveComparisonUseCase, RetrieveComparisonContributionsUseCase, CreateComparisonUseCase, PublishComparisonUseCase

interface VisualizationUseCases : RetrieveVisualizationUseCase
