package eu.tib.orkg.prototype.contenttypes.api

interface PaperUseCases : RetrievePaperUseCase

interface ContributionUseCases : RetrieveContributionUseCase

interface ComparisonUseCases : RetrieveComparisonUseCase, RetrieveComparisonContributionsUseCase

interface VisualizationUseCases : RetrieveVisualizationUseCase
