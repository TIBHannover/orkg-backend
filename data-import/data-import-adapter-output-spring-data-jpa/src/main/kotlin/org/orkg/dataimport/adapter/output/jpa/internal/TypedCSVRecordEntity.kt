package org.orkg.dataimport.adapter.output.jpa.internal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.orkg.common.deserializeToObject
import org.orkg.dataimport.domain.TypedValue
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.TypedCSVRecord
import java.util.UUID

@Entity
@Table(name = "typed_csv_records")
class TypedCSVRecordEntity {
    @Id
    var id: UUID? = null

    @Column(name = "csv_id", nullable = false)
    var csvId: UUID? = null

    @Column(name = "item_number", nullable = false)
    var itemNumber: Long? = null

    @Column(name = "line_number", nullable = false)
    var lineNumber: Long? = null

    @Column(nullable = false)
    var values: ByteArray? = null

    fun toTypedCSVRecord() =
        TypedCSVRecord(
            id = id!!,
            csvId = CSVID(csvId!!),
            itemNumber = itemNumber!!,
            lineNumber = lineNumber!!,
            values = values?.deserializeToObject<List<TypedValue>>().orEmpty(),
        )
}
