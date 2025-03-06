package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.Either
import org.orkg.contenttypes.domain.actions.ThingIdValidator
import org.orkg.contenttypes.input.RowDefinition
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ThingRepository

class AbstractTableCellsValidator(
    override val thingRepository: ThingRepository,
) : ThingIdValidator {
    internal fun validate(
        rows: List<RowDefinition>,
        tempIds: Set<String>,
        validationCacheIn: Map<String, Either<String, Thing>>,
    ): Map<String, Either<String, Thing>> {
        val validationCache = validationCacheIn.toMutableMap()
        rows.asSequence().drop(1).forEach { row ->
            row.data.forEach { id ->
                if (id != null) {
                    validateId(id, tempIds, validationCache)
                }
            }
        }
        return validationCache
    }
}
