package org.orkg.common.configuration

import com.fasterxml.jackson.databind.JsonMappingException
import org.orkg.common.exceptions.escapeJsonPointerReferenceToken

@Deprecated("To be removed")
internal val JsonMappingException.fieldPath: String
    get() = path.joinToString(prefix = "$", separator = "") {
        with(it) {
            when {
                fieldName != null -> ".$fieldName"
                index >= 0 -> "[$index]"
                else -> ".?"
            }
        }
    }

internal val JsonMappingException.jsonPointer: String
    get() = path.joinToString(prefix = "#/", separator = "/") {
        with(it) {
            when {
                fieldName != null -> escapeJsonPointerReferenceToken(fieldName)
                index >= 0 -> "$index"
                else -> "?"
            }
        }
    }
