package org.orkg.dataimport.domain.csv

import org.orkg.dataimport.domain.TypedValue
import java.io.Serial
import java.io.Serializable
import java.util.UUID

data class TypedCSVRecord(
    val id: UUID,
    val csvId: CSVID,
    val itemNumber: Long,
    val lineNumber: Long,
    val values: List<TypedValue>,
) : Serializable {
    init {
        require(values is Serializable)
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = -8788569255930986570L
    }
}
