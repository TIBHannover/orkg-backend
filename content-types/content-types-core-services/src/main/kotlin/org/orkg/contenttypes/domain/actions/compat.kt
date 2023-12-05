package org.orkg.contenttypes.domain.actions

import org.orkg.contenttypes.domain.actions.comparison.ComparisonAction
import org.orkg.contenttypes.domain.actions.contribution.ContributionAction
import org.orkg.contenttypes.domain.actions.paper.PaperAction
import org.orkg.contenttypes.domain.actions.template.TemplateAction
import org.orkg.contenttypes.domain.actions.template.property.TemplatePropertyAction
import org.orkg.contenttypes.domain.actions.visualization.VisualizationAction
import org.orkg.contenttypes.input.CreateComparisonUseCase
import org.orkg.contenttypes.input.CreateContributionUseCase
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase
import org.orkg.contenttypes.input.CreateTemplateUseCase
import org.orkg.contenttypes.input.CreateVisualizationUseCase

internal typealias BakedStatement = Triple<String, String, String>

internal typealias CreatePaperCommand = CreatePaperUseCase.CreateCommand
internal typealias CreateContributionCommand = CreateContributionUseCase.CreateCommand
internal typealias CreateComparisonCommand = CreateComparisonUseCase.CreateCommand
internal typealias CreateVisualizationCommand = CreateVisualizationUseCase.CreateCommand
internal typealias CreateTemplateCommand = CreateTemplateUseCase.CreateCommand
internal typealias CreateTemplatePropertyCommand = CreateTemplatePropertyUseCase.CreateCommand

internal typealias PaperState = PaperAction.State
internal typealias ContributionState = ContributionAction.State
internal typealias ComparisonState = ComparisonAction.State
internal typealias VisualizationState = VisualizationAction.State
internal typealias TemplateState = TemplateAction.State
internal typealias TemplatePropertyState = TemplatePropertyAction.State
