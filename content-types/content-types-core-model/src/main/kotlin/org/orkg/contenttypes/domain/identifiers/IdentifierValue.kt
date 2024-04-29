package org.orkg.contenttypes.domain.identifiers

import dev.forkhandles.values.Value

interface IdentifierValue : Value<String> {
    val uri: String
}
