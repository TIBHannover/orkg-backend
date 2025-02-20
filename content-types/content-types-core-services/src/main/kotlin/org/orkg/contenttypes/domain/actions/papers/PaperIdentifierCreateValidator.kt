package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.IdentifierValidator
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.StatementRepository

class PaperIdentifierCreateValidator(
    statementRepository: StatementRepository,
) : IdentifierValidator(statementRepository),
    Action<CreatePaperCommand, CreatePaperState> {
    override fun invoke(command: CreatePaperCommand, state: CreatePaperState): CreatePaperState =
        state.apply { validate(command.identifiers, Classes.paper, null, PaperAlreadyExists::withIdentifier) }
}
