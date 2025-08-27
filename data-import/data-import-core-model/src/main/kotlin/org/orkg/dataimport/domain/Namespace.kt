package org.orkg.dataimport.domain

import org.orkg.common.ThingId

data class Namespace(
    val name: String,
    val closed: Boolean,
    val properties: Map<String, Property> = emptyMap(),
    val columnValueType: ThingId? = null,
    val columnValueConstraint: ((String) -> Unit)? = null,
    val headerValueValidator: ((String) -> Unit)? = null,
) {
    fun typeForValue(value: String?): ThingId? =
        value?.let { properties[value]?.type } ?: columnValueType
}
