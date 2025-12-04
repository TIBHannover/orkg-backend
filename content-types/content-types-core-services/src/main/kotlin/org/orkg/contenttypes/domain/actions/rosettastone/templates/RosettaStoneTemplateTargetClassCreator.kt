package org.orkg.contenttypes.domain.actions.rosettastone.templates

import org.orkg.contenttypes.domain.actions.CreateRosettaStoneTemplateCommand
import org.orkg.contenttypes.domain.actions.rosettastone.templates.CreateRosettaStoneTemplateAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassHierarchyUseCases
import org.orkg.graph.input.CreateClassHierarchyUseCase
import org.orkg.graph.input.CreateClassUseCase
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateStatementUseCase.CreateCommand
import org.orkg.graph.input.UnsafeClassUseCases
import org.orkg.graph.input.UnsafeLiteralUseCases
import org.orkg.graph.input.UnsafeStatementUseCases

class RosettaStoneTemplateTargetClassCreator(
    private val unsafeClassUseCases: UnsafeClassUseCases,
    private val unsafeStatementUseCases: UnsafeStatementUseCases,
    private val unsafeLiteralUseCases: UnsafeLiteralUseCases,
    private val classHierarchyUseCases: ClassHierarchyUseCases,
) : CreateRosettaStoneTemplateAction {
    override fun invoke(command: CreateRosettaStoneTemplateCommand, state: State): State {
        val classId = unsafeClassUseCases.create(
            CreateClassUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = "${command.label} (class)",
            )
        )
        unsafeStatementUseCases.create(
            CreateCommand(
                contributorId = command.contributorId,
                subjectId = state.rosettaStoneTemplateId!!,
                predicateId = Predicates.shTargetClass,
                objectId = classId
            )
        )
        val exampleUsageId = unsafeLiteralUseCases.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = command.exampleUsage
            )
        )
        unsafeStatementUseCases.create(
            CreateCommand(
                contributorId = command.contributorId,
                subjectId = classId,
                predicateId = Predicates.exampleOfUsage,
                objectId = exampleUsageId
            )
        )
        val descriptionId = unsafeLiteralUseCases.create(
            CreateLiteralUseCase.CreateCommand(
                contributorId = command.contributorId,
                label = "${command.description}\n\nThis is a Rosetta Statement class. Every Rosetta Stone Statement class has a template associated that should be used when adding a statement of this type to the ORKG."
            )
        )
        unsafeStatementUseCases.create(
            CreateCommand(
                contributorId = command.contributorId,
                subjectId = classId,
                predicateId = Predicates.description,
                objectId = descriptionId
            )
        )
        classHierarchyUseCases.create(
            CreateClassHierarchyUseCase.CreateCommand(
                parentId = Classes.rosettaStoneStatement,
                childIds = setOf(classId),
                contributorId = command.contributorId,
            )
        )
        return state
    }
}
