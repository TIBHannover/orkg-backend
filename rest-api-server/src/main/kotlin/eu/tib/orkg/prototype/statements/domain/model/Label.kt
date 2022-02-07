package eu.tib.orkg.prototype.statements.domain.model

import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.Validation
import dev.forkhandles.values.Value

@JvmInline
value class Label private constructor(override val value: String) : Value<String> {
    companion object : StringValueFactory<Label>(::Label, isValidLabel)
}

private val isValidLabel: Validation<String> = { it.isNotBlank().and(it.contains("\n").not()) }
