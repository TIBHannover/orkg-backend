package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.IdentifierValidator
import org.orkg.contenttypes.domain.actions.UpdatePaperCommand
import org.orkg.contenttypes.domain.actions.papers.UpdatePaperAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.StatementRepository

class PaperIdentifierUpdateValidator(
    private val identifierValidator: IdentifierValidator,
) : UpdatePaperAction {
    constructor(statementRepository: StatementRepository) : this(IdentifierValidator(statementRepository))

    override fun invoke(command: UpdatePaperCommand, state: State): State {
        if (command.identifiers != null && command.identifiers != state.paper!!.identifiers) {
            identifierValidator.validate(
                identifiers = command.identifiers!!,
                `class` = Classes.paper,
                subjectId = command.paperId,
                exceptionFactory = PaperAlreadyExists::withIdentifier
            )
        }
        return state
    }
}
