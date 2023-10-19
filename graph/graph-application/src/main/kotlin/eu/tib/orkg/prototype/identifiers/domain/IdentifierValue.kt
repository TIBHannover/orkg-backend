package eu.tib.orkg.prototype.identifiers.domain

import dev.forkhandles.values.Value

interface IdentifierValue : Value<String> {
    val uri: String
}
