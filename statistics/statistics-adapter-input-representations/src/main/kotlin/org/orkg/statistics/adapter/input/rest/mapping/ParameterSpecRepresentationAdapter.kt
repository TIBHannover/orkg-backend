package org.orkg.statistics.adapter.input.rest.mapping

import org.orkg.statistics.adapter.input.rest.ParameterSpecRepresentation
import org.orkg.statistics.domain.MultiValueParameterSpec
import org.orkg.statistics.domain.ParameterSpec
import org.orkg.statistics.domain.SingleValueParameterSpec

interface ParameterSpecRepresentationAdapter {
    fun ParameterSpec<*>.toParameterSpecRepresentation(id: String): ParameterSpecRepresentation<*> =
        when (this) {
            is SingleValueParameterSpec -> ParameterSpecRepresentation(
                id = id,
                name = name,
                description = description,
                type = type.simpleName ?: "String",
                multivalued = false,
                values = values
            )
            is MultiValueParameterSpec<*> -> ParameterSpecRepresentation(
                id = id,
                name = name,
                description = description,
                type = type.simpleName ?: "String",
                multivalued = true,
                values = values
            )
        }
}
