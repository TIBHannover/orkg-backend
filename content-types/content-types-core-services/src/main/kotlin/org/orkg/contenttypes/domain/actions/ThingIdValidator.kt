package org.orkg.contenttypes.domain.actions

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ThingNotDefined
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.output.ThingRepository

class ThingIdValidator(
    private val thingRepository: ThingRepository,
) {
    internal fun validate(
        id: String,
        thingCommands: Map<String, CreateThingCommandPart>,
        validationCache: MutableMap<String, Either<CreateThingCommandPart, Thing>>,
    ): Either<CreateThingCommandPart, Thing> =
        validationCache.getOrPut(id) {
            if (id.isTempId) {
                Either.left(thingCommands[id] ?: throw ThingNotDefined(id))
            } else {
                thingRepository.findById(ThingId(id))
                    .map { Either.right<CreateThingCommandPart, Thing>(it) }
                    .orElseThrow { ThingNotFound(id) }
            }
        }
}
