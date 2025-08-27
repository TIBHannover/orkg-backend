package org.orkg.dataimport.domain

import org.orkg.common.ThingId
import java.io.Serial
import java.io.Serializable

data class TypedValue(
    val namespace: String?,
    val value: String?,
    val type: ThingId,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -3516974126547996926L
    }
}
