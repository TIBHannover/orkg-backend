package org.orkg.contenttypes.domain.actions.tables

import org.orkg.common.Either
import org.orkg.contenttypes.domain.TableHeaderValueMustBeLiteral
import org.orkg.contenttypes.domain.actions.ThingIdValidator
import org.orkg.contenttypes.input.CreateLiteralCommandPart
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.RowCommand
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ThingRepository

class AbstractTableColumnsValidator(
    override val thingRepository: ThingRepository,
) : ThingIdValidator {
    internal fun validate(
        thingsCommand: Map<String, CreateThingCommandPart>,
        rows: List<RowCommand>,
        tempIds: Set<String>,
        validationCacheIn: Map<String, Either<String, Thing>>,
    ): Map<String, Either<String, Thing>> {
        val validationCache = validationCacheIn.toMutableMap()
        rows.first().data.forEachIndexed { index, id ->
            val `object` = validateId(id!!, tempIds, validationCache)

            `object`.onLeft { tempId ->
                val command = thingsCommand[tempId]!!
                if (command !is CreateLiteralCommandPart || Literals.XSD.fromString(command.dataType) != Literals.XSD.STRING) {
                    throw TableHeaderValueMustBeLiteral(index)
                }
            }

            `object`.onRight { thing ->
                if (thing !is Literal || Literals.XSD.fromString(thing.datatype) != Literals.XSD.STRING) {
                    throw TableHeaderValueMustBeLiteral(index)
                }
            }
        }
        return validationCache
    }
}
