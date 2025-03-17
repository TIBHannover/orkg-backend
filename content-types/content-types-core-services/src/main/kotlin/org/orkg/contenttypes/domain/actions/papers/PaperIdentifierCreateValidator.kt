package org.orkg.contenttypes.domain.actions.papers

import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.actions.CreatePaperCommand
import org.orkg.contenttypes.domain.actions.IdentifierValidator
import org.orkg.contenttypes.domain.actions.papers.CreatePaperAction.State
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.StatementRepository

class PaperIdentifierCreateValidator(
    private val identifierValidator: IdentifierValidator,
) : CreatePaperAction {
    constructor(statementRepository: StatementRepository) : this(IdentifierValidator(statementRepository))

    override fun invoke(command: CreatePaperCommand, state: State): State {
        identifierValidator.validate(
            identifiers = command.identifiers,
            `class` = Classes.paper,
            subjectId = null,
            exceptionFactory = PaperAlreadyExists::withIdentifier
        )
        return state
    }
}
