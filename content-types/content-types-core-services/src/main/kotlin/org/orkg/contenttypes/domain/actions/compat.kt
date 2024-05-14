package org.orkg.contenttypes.domain.actions

import org.orkg.contenttypes.domain.actions.comparisons.CreateComparisonAction
import org.orkg.contenttypes.domain.actions.comparisons.UpdateComparisonAction
import org.orkg.contenttypes.domain.actions.contributions.ContributionAction
import org.orkg.contenttypes.domain.actions.literaturelists.CreateLiteratureListAction
import org.orkg.contenttypes.domain.actions.literaturelists.UpdateLiteratureListAction
import org.orkg.contenttypes.domain.actions.literaturelists.sections.CreateLiteratureListSectionAction
import org.orkg.contenttypes.domain.actions.literaturelists.sections.UpdateLiteratureListSectionAction
import org.orkg.contenttypes.domain.actions.papers.CreatePaperAction
import org.orkg.contenttypes.domain.actions.papers.UpdatePaperAction
import org.orkg.contenttypes.domain.actions.rosettastone.statements.CreateRosettaStoneStatementAction
import org.orkg.contenttypes.domain.actions.rosettastone.templates.CreateRosettaStoneTemplateAction
import org.orkg.contenttypes.domain.actions.templates.CreateTemplateAction
import org.orkg.contenttypes.domain.actions.templates.UpdateTemplateAction
import org.orkg.contenttypes.domain.actions.templates.instances.UpdateTemplateInstanceAction
import org.orkg.contenttypes.domain.actions.templates.properties.CreateTemplatePropertyAction
import org.orkg.contenttypes.domain.actions.templates.properties.UpdateTemplatePropertyAction
import org.orkg.contenttypes.domain.actions.visualizations.VisualizationAction
import org.orkg.contenttypes.input.CreateComparisonUseCase
import org.orkg.contenttypes.input.CreateContributionUseCase
import org.orkg.contenttypes.input.CreateLiteratureListSectionUseCase
import org.orkg.contenttypes.input.CreateLiteratureListUseCase
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.CreateRosettaStoneStatementUseCase
import org.orkg.contenttypes.input.CreateRosettaStoneTemplateUseCase
import org.orkg.contenttypes.input.CreateTemplatePropertyUseCase
import org.orkg.contenttypes.input.CreateTemplateUseCase
import org.orkg.contenttypes.input.CreateVisualizationUseCase
import org.orkg.contenttypes.input.UpdateComparisonUseCase
import org.orkg.contenttypes.input.UpdateLiteratureListSectionUseCase
import org.orkg.contenttypes.input.UpdateLiteratureListUseCase
import org.orkg.contenttypes.input.UpdatePaperUseCase
import org.orkg.contenttypes.input.UpdateTemplateInstanceUseCase
import org.orkg.contenttypes.input.UpdateTemplatePropertyUseCase
import org.orkg.contenttypes.input.UpdateTemplateUseCase

internal typealias BakedStatement = Triple<String, String, String>

internal typealias CreatePaperCommand = CreatePaperUseCase.CreateCommand
internal typealias CreateContributionCommand = CreateContributionUseCase.CreateCommand
internal typealias CreateComparisonCommand = CreateComparisonUseCase.CreateCommand
internal typealias CreateVisualizationCommand = CreateVisualizationUseCase.CreateCommand
internal typealias CreateTemplateCommand = CreateTemplateUseCase.CreateCommand
internal typealias CreateTemplatePropertyCommand = CreateTemplatePropertyUseCase.CreateCommand
internal typealias CreateLiteratureListCommand = CreateLiteratureListUseCase.CreateCommand
internal typealias CreateLiteratureListSectionCommand = CreateLiteratureListSectionUseCase.CreateCommand
internal typealias CreateRosettaStoneTemplateCommand = CreateRosettaStoneTemplateUseCase.CreateCommand
internal typealias CreateRosettaStoneStatementCommand = CreateRosettaStoneStatementUseCase.CreateCommand

internal typealias UpdatePaperCommand = UpdatePaperUseCase.UpdateCommand
internal typealias UpdateComparisonCommand = UpdateComparisonUseCase.UpdateCommand
internal typealias UpdateTemplateCommand = UpdateTemplateUseCase.UpdateCommand
internal typealias UpdateTemplatePropertyCommand = UpdateTemplatePropertyUseCase.UpdateCommand
internal typealias UpdateTemplateInstanceCommand = UpdateTemplateInstanceUseCase.UpdateCommand
internal typealias UpdateLiteratureListCommand = UpdateLiteratureListUseCase.UpdateCommand
internal typealias UpdateLiteratureListSectionCommand = UpdateLiteratureListSectionUseCase.UpdateCommand

internal typealias CreatePaperState = CreatePaperAction.State
internal typealias UpdatePaperState = UpdatePaperAction.State

internal typealias CreateTemplateState = CreateTemplateAction.State
internal typealias UpdateTemplateState = UpdateTemplateAction.State

internal typealias CreateTemplatePropertyState = CreateTemplatePropertyAction.State
internal typealias UpdateTemplatePropertyState = UpdateTemplatePropertyAction.State

internal typealias CreateRosettaStoneTemplateState = CreateRosettaStoneTemplateAction.State

internal typealias CreateRosettaStoneStatementState = CreateRosettaStoneStatementAction.State

internal typealias CreateComparisonState = CreateComparisonAction.State
internal typealias UpdateComparisonState = UpdateComparisonAction.State

internal typealias ContributionState = ContributionAction.State
internal typealias VisualizationState = VisualizationAction.State

internal typealias UpdateTemplateInstanceState = UpdateTemplateInstanceAction.State

internal typealias CreateLiteratureListState = CreateLiteratureListAction.State
internal typealias UpdateLiteratureListState = UpdateLiteratureListAction.State

internal typealias CreateLiteratureListSectionState = CreateLiteratureListSectionAction.State
internal typealias UpdateLiteratureListSectionState = UpdateLiteratureListSectionAction.State
