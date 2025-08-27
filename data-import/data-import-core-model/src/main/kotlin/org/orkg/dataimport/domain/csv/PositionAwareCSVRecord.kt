package org.orkg.dataimport.domain.csv

import org.apache.commons.csv.CSVRecord

data class PositionAwareCSVRecord(
    val record: CSVRecord,
    val itemNumber: Long,
    val lineNumber: Long,
)
