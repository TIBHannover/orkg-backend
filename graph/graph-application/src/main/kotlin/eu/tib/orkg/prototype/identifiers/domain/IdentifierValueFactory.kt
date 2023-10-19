package eu.tib.orkg.prototype.identifiers.domain

import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.Value

open class IdentifierValueFactory<DOMAIN : Value<String>>(
    fn: (String) -> DOMAIN,
    validationRegex: Regex,
    uriValidationRegex: Regex
) : StringValueFactory<DOMAIN>(
    { value -> fn(uriValidationRegex.matchAt(value, 0)?.let { it.groups[1]!!.value } ?: value) },
    { value -> validationRegex.matches(value) || uriValidationRegex.matches(value) }
)
