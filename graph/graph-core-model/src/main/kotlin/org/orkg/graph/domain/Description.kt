package org.orkg.graph.domain

import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.Validation
import dev.forkhandles.values.Value

@JvmInline
value class Description private constructor(override val value: String) : Value<String> {
    companion object : StringValueFactory<Description>(::Description, isValidDescription)
}

private val isValidDescription: Validation<String> = {
    it.isEmpty().or(it.isNotBlank().and(it.contains("\u0000").not()).and(it.length <= MAX_LABEL_LENGTH))
}
