package org.orkg.dataimport.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.orkg.common.ThingId
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordImportResult
import org.orkg.dataimport.domain.csv.papers.PaperCSVRecordImportResult.Type
import java.util.UUID

@Entity
@Table(name = "paper_csv_import_result_records")
class PaperCSVRecordImportResultEntity {
    @Id
    @Column(nullable = false)
    var id: UUID? = null

    @Column(name = "imported_entity_id", nullable = false)
    var importedEntityId: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "imported_entity_type", nullable = false)
    var importedEntityType: Type? = null

    @Column(name = "csv_id", nullable = false)
    var csvId: UUID? = null

    @Column(name = "item_number", nullable = false)
    var itemNumber: Long? = null

    @Column(name = "line_number", nullable = false)
    var lineNumber: Long? = null

    fun toPaperCSVRecordImportResult() =
        PaperCSVRecordImportResult(
            id = id!!,
            importedEntityId = ThingId(importedEntityId!!),
            importedEntityType = importedEntityType!!,
            csvId = CSVID(csvId!!),
            itemNumber = itemNumber!!,
            lineNumber = lineNumber!!,
        )
}
