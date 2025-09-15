package org.orkg.graph.domain

import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.Validation
import dev.forkhandles.values.Value

const val MAX_LABEL_LENGTH = 8164 // defined by the maximum supported key-length of a neo4j btree index

@JvmInline
value class Label private constructor(override val value: String) : Value<String> {
    companion object : StringValueFactory<Label>(::Label, isValidLabel)
}

private val isValidLabel: Validation<String> = {
    it.isEmpty().or(it.isNotBlank().and(it.contains("\n").not()).and(it.contains("\u0000").not()).and(it.length <= MAX_LABEL_LENGTH))
}
