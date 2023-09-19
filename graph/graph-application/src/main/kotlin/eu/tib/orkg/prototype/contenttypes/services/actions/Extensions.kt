package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.shared.mapValuesNotNull
import eu.tib.orkg.prototype.statements.domain.model.ThingId

internal val String.isTempId: Boolean get() = startsWith('#') || startsWith('^')

internal infix fun Map<ThingId, String>.associateWith(
    identifierToValue: Map<String, String>
): Map<ThingId, String> =
    mapValuesNotNull {
        if (it.value in identifierToValue) {
            val value = identifierToValue[it.value]
            if (!value.isNullOrBlank()) {
                return@mapValuesNotNull value
            }
        }
        return@mapValuesNotNull null
    }
