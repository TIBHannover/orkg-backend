package org.orkg.dataimport.domain.testing.fixtures

import org.orkg.common.ContributorId
import org.orkg.common.testing.fixtures.Assets.csv
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.domain.csv.CSV.Format
import org.orkg.dataimport.domain.csv.CSV.State
import org.orkg.dataimport.domain.csv.CSV.Type
import org.orkg.dataimport.domain.csv.CSVID
import java.time.OffsetDateTime

fun createCSV() = CSV(
    id = CSVID("bf59dd89-6a4b-424b-b9d5-36042661e837"),
    name = "papers.csv",
    type = Type.PAPER,
    format = Format.DEFAULT,
    state = State.UPLOADED,
    validationJobId = null,
    importJobId = null,
    data = csv("papers"),
    createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    createdAt = OffsetDateTime.parse("2024-04-30T16:22:58.959539600+02:00"),
)
