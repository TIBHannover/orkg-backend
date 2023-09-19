package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.shared.Either
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId

interface PaperAction {
    operator fun invoke(command: CreatePaperCommand, state: State): State

    data class State(
        val tempIds: Set<String> = emptySet(),
        val validatedIds: Map<String, Either<String, Thing>> = emptyMap(),
        val bakedStatements: Set<BakedStatement> = emptySet(),
        val authors: List<Author> = emptyList(),
        val paperId: ThingId? = null
    )
}
