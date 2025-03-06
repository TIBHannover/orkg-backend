package org.orkg.statistics.adapter.input.rest.mapping

import org.orkg.statistics.adapter.input.rest.ParameterSpecRepresentation
import org.orkg.statistics.domain.ParameterSpec

interface ParameterSpecRepresentationAdapter {
    fun ParameterSpec<*>.toParameterSpecRepresentation(id: String): ParameterSpecRepresentation<*> =
        ParameterSpecRepresentation(id, name, description, type.simpleName ?: "String", values)
}
