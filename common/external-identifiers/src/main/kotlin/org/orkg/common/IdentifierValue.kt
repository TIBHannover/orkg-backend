package org.orkg.common

import dev.forkhandles.values.Value

interface IdentifierValue : Value<String> {
    val uri: String
}
