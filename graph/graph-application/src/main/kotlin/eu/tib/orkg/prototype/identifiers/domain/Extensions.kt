package eu.tib.orkg.prototype.identifiers.domain

import eu.tib.orkg.prototype.identifiers.application.InvalidIdentifier

fun Set<Identifier>.parse(identifierToValue: Map<String, String>, validate: Boolean = true): Map<Identifier, String> =
    mapNotNull { identifier ->
        if (identifier.id in identifierToValue) {
            val value = identifierToValue[identifier.id]
            if (!value.isNullOrBlank()) {
                if (validate) {
                    try {
                        identifier.newInstance(value)
                    } catch (e: IllegalArgumentException) {
                        throw InvalidIdentifier(identifier.id, e)
                    }
                }
                return@mapNotNull identifier to value
            }
        }
        return@mapNotNull null
    }.toMap()
