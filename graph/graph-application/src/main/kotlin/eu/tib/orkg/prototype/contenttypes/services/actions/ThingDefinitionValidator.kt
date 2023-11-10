package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.api.CreatePaperUseCase
import eu.tib.orkg.prototype.contenttypes.application.ThingIsNotAClass
import eu.tib.orkg.prototype.contenttypes.services.actions.contribution.ContributionAction
import eu.tib.orkg.prototype.contenttypes.services.actions.paper.PaperAction
import eu.tib.orkg.prototype.shared.Either
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.spi.ThingRepository

abstract class ThingDefinitionValidator(
    override val thingRepository: ThingRepository
) : ThingIdValidator {
    internal fun validateIdsInDefinitions(
        contents: CreatePaperUseCase.CreateCommand.PaperContents,
        tempIds: Set<String>,
        validatedIds: MutableMap<String, Either<String, Thing>>
    ) {
        contents.lists.values
            .flatMap { it.elements }
            .forEach { validateId(it, tempIds, validatedIds) }
        contents.resources.values
            .flatMap { it.classes }
            .toSet()
            .forEach {
                validateId(it.value, tempIds, validatedIds).onRight { thing ->
                    if (thing !is Class) {
                        throw ThingIsNotAClass(thing.id)
                    }
                }
            }
    }
}
