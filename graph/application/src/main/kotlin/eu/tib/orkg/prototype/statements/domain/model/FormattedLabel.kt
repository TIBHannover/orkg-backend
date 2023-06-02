package eu.tib.orkg.prototype.statements.domain.model

import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.Validation
import dev.forkhandles.values.Value

@JvmInline
value class FormattedLabel private constructor(override val value: String) : Value<String> {
    companion object : StringValueFactory<FormattedLabel>(::FormattedLabel, isValidLabel)
}

private val isValidLabel: Validation<String> = { it.isEmpty().or(it.isNotBlank()) }
