package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.application.ThingNotDefined
import eu.tib.orkg.prototype.shared.Either
import eu.tib.orkg.prototype.statements.application.ThingNotFound
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ThingRepository

interface ThingIdValidator {
    val thingRepository: ThingRepository

    fun validateId(
        id: String,
        tempIds: Set<String>,
        validationCache: MutableMap<String, Either<String, Thing>>
    ): Either<String, Thing> =
        validationCache.getOrPut(id) {
            if (id.isTempId) {
                if (id !in tempIds) {
                    throw ThingNotDefined(id)
                }
                Either.left(id)
            } else {
                thingRepository.findByThingId(ThingId(id))
                    .map { Either.right<String, Thing>(it) }
                    .orElseThrow { ThingNotFound(id) }
            }
        }
}
