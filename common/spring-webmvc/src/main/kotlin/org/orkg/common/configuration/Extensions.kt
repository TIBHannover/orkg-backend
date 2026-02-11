package org.orkg.common.configuration

import org.orkg.common.exceptions.escapeJsonPointerReferenceToken
import tools.jackson.core.JacksonException

@Deprecated("To be removed")
internal val JacksonException.fieldPath: String
    get() = path.joinToString(prefix = "$", separator = "") {
        with(it) {
            when {
                propertyName != null -> ".$propertyName"
                index >= 0 -> "[$index]"
                else -> ".?"
            }
        }
    }

internal val JacksonException.jsonPointer: String
    get() = path.joinToString(prefix = "#/", separator = "/") {
        with(it) {
            when {
                propertyName != null -> escapeJsonPointerReferenceToken(propertyName)
                index >= 0 -> "$index"
                else -> "?"
            }
        }
    }
