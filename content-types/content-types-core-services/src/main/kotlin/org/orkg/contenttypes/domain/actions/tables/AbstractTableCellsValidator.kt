package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.Either
import org.orkg.contenttypes.domain.actions.ThingIdValidator
import org.orkg.contenttypes.input.CreateRowCommand
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ThingRepository

class AbstractTableCellsValidator(
    private val thingIdValidator: ThingIdValidator,
) {
    constructor(thingRepository: ThingRepository) : this(ThingIdValidator(thingRepository))

    internal fun validate(
        rows: List<CreateRowCommand>,
        thingCommands: Map<String, CreateThingCommandPart>,
        validationCacheIn: Map<String, Either<CreateThingCommandPart, Thing>>,
    ): Map<String, Either<CreateThingCommandPart, Thing>> {
        val validationCache = validationCacheIn.toMutableMap()
        rows.asSequence().drop(1).forEach { row ->
            row.data.forEach { id ->
                if (id != null) {
                    thingIdValidator.validate(id, thingCommands, validationCache)
                }
            }
        }
        return validationCache
    }
}
