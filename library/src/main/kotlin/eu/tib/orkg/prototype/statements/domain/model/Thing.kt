package eu.tib.orkg.prototype.statements.domain.model

import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.Validation
import dev.forkhandles.values.Value

sealed interface Thing {
    val thingId: ThingId
    val label: String
}

@JvmInline
value class ThingId private constructor(override val value: String) : Value<String> {
    companion object : StringValueFactory<ThingId>(::ThingId, { it.isNotBlank().and(it.matches(VALID_ID_REGEX)) })
}

val x: Validation<String> = { false }

@Suppress("RegExpSimplifiable")
val VALID_ID_REGEX: Regex = """^[a-zA-Z0-9:_-]+$""".toRegex()
