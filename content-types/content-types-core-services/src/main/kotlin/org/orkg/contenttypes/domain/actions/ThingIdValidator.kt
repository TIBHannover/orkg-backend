package org.orkg.contenttypes.domain.actions

import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ThingNotDefined
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.output.ThingRepository

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
