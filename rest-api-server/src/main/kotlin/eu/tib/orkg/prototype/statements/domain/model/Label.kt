package eu.tib.orkg.prototype.statements.domain.model

import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.Value

@JvmInline
value class Label private constructor(override val value: String) : Value<String> {
    companion object : StringValueFactory<Label>(::Label, String::isNotBlankAndDoesNotContainNewlines)
}

private fun String.isNotBlankAndDoesNotContainNewlines(): Boolean = this.isNotBlank().and(this.contains("\n").not())
