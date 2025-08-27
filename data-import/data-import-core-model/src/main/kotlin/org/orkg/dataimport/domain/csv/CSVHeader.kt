package org.orkg.dataimport.domain.csv

import org.orkg.common.ThingId
import java.io.Serial
import java.io.Serializable

data class CSVHeader(
    val column: Long,
    val name: String,
    val namespace: String?,
    val columnType: ThingId?,
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -1610322796086073990L
    }
}
