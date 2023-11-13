package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.api.CreateComparisonUseCase
import eu.tib.orkg.prototype.contenttypes.api.CreateContributionUseCase
import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.services.actions.comparison.ComparisonAction
import eu.tib.orkg.prototype.contenttypes.services.actions.contribution.ContributionAction
import eu.tib.orkg.prototype.contenttypes.services.actions.paper.PaperAction

internal typealias BakedStatement = Triple<String, String, String>

internal typealias CreatePaperCommand = CreatePaperUseCase.CreateCommand
internal typealias CreateContributionCommand = CreateContributionUseCase.CreateCommand
internal typealias CreateComparisonCommand = CreateComparisonUseCase.CreateCommand

internal typealias PaperState = PaperAction.State
internal typealias ContributionState = ContributionAction.State
internal typealias ComparisonState = ComparisonAction.State
