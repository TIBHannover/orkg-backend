package org.orkg.dataimport.adapter.input.rest.mapping

import org.orkg.dataimport.adapter.input.rest.TypedValueRepresentation
import org.orkg.dataimport.domain.TypedValue

interface TypedValueRepresentationAdapter {
    fun TypedValue.toTypedValueRepresentation(): TypedValueRepresentation =
        TypedValueRepresentation(namespace, value, type)
}
