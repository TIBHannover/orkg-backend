package org.orkg.contenttypes.domain.actions.paper

import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.Action
import org.orkg.contenttypes.domain.actions.IdentifierValidator
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.UpdatePaperState
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.StatementRepository

class PaperIdentifierUpdateValidator(
    statementRepository: StatementRepository
) : IdentifierValidator(statementRepository), Action<UpdatePaperCommand, UpdatePaperState> {
    override fun invoke(command: UpdatePaperCommand, state: UpdatePaperState): UpdatePaperState {
        if (command.identifiers != null && command.identifiers != state.paper!!.identifiers) {
            validate(command.identifiers!!, Classes.paper, command.paperId, PaperAlreadyExists::withIdentifier)
        }
        return state
    }
}
