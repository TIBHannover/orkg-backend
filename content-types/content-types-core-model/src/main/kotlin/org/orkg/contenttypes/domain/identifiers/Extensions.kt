package org.orkg.contenttypes.domain.identifiers

fun Set<Identifier>.parse(
    identifierToValues: Map<String, List<String>>,
    validate: Boolean = true
): Map<Identifier, List<String>> =
    mapNotNull { identifier ->
        if (identifier.id in identifierToValues) {
            val values = identifierToValues[identifier.id]!!
                .filter { it.isNotBlank() }
                .distinct()
            if (validate) {
                values.forEach { value ->
                    try {
                        identifier.newInstance(value)
                    } catch (e: IllegalArgumentException) {
                        throw InvalidIdentifier(identifier.id, e)
                    }
                }
            }
            return@mapNotNull identifier to values
        }
        return@mapNotNull null
    }.toMap()
