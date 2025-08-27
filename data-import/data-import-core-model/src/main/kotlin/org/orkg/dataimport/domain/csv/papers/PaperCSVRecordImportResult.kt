package org.orkg.dataimport.domain.csv.papers

import org.orkg.common.ThingId
import org.orkg.dataimport.domain.csv.CSVID
import java.io.Serial
import java.io.Serializable
import java.util.UUID

data class PaperCSVRecordImportResult(
    val id: UUID,
    val importedEntityId: ThingId,
    val importedEntityType: Type,
    val csvId: CSVID,
    val itemNumber: Long,
    val lineNumber: Long,
) : Serializable {
    enum class Type {
        PAPER,
        CONTRIBUTION,
    }

    companion object {
        @Serial
        private const val serialVersionUID: Long = 2830549422951730202L
    }
}
